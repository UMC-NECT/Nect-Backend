package com.nect.api.domain.home.service;

import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.user.User;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectTeamRoleRepository;
import com.nect.core.repository.user.ProjectUserRepositoryComplete;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 홈화면에 필요한 정보를 가져오는 service입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeProjectQueryService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepositoryComplete projectUserRepositoryComplete;
    private final ProjectTeamRoleRepository projectTeamRoleRepository;
    private final S3Service s3Service;

    public record HomeProjectBatch(
            Map<Long, User> authorByProjectId,
            Map<Long, Integer> activeCountByProjectId,
            Map<Long, Integer> maxMemberCountByProjectId,
            Map<Long, Map<String, Integer>> partCountsByProjectId
    ) {}

    public HomeProjectBatch loadHomeProjectBatch(List<Project> projects) {
        if (projects.isEmpty()) {
            return new HomeProjectBatch(Map.of(), Map.of(), Map.of(), Map.of());
        }

        List<Long> projectIds = projects.stream().map(Project::getId).toList();

        Map<Long, Long> leaderUserIdByProjectId = projectUserRepository.findLeadersByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        ProjectUserRepository.ProjectLeaderRow::getProjectId,
                        ProjectUserRepository.ProjectLeaderRow::getLeaderUserId
                ));

        List<Long> leaderUserIds = leaderUserIdByProjectId.values().stream().distinct().toList();
        Map<Long, User> userById = userRepository.findByUserIdIn(leaderUserIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Long, User> authorByProjectId = leaderUserIdByProjectId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> userById.get(e.getValue())
                ));

        Map<Long, Integer> activeCountByProjectId = projectUserRepository.countActiveMembersByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        ProjectUserRepository.ProjectActiveCountRow::getProjectId,
                        r -> r.getActiveCount().intValue()
                ));

        Map<Long, Integer> maxMemberCountByProjectId = recruitmentRepository.sumCapacityByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        RecruitmentRepository.ProjectCapacityRow::getProjectId,
                        r -> r.getCapacitySum() == null ? 0 : r.getCapacitySum()
                ));

        Map<Long, Map<String, Integer>> partCountsByProjectId = new HashMap<>();
        for (Recruitment recruitment : recruitmentRepository.findAllByProject_IdIn(projectIds)) {
            Integer capacity = recruitment.getCapacity();

            if (capacity == null || capacity <= 0) {
                continue;
            }

            RoleField field = recruitment.getField();
            String roleKey;
            if (field == RoleField.CUSTOM) {
                String customField = recruitment.getCustomField();
                roleKey = (customField == null || customField.isBlank())
                        ? RoleField.CUSTOM.name()
                        : customField;
            } else {
                roleKey = field.name();
            }

            partCountsByProjectId
                    .computeIfAbsent(recruitment.getProject().getId(), k -> new HashMap<>())
                    .merge(roleKey, capacity, Integer::sum);
        }

        return new HomeProjectBatch(
                authorByProjectId,
                activeCountByProjectId,
                maxMemberCountByProjectId,
                partCountsByProjectId
        );
    }

    public List<Project> getProjects(Long userId, PageRequest pageRequest){
        return (userId == null)
                ? projectRepository.findHomeProjectsWithoutUser(RecruitmentStatus.OPEN, pageRequest)
                : projectRepository.findHomeProjects(userId, RecruitmentStatus.OPEN, pageRequest);
    }

    public List<Project> getFilteredProjects(Long userId, PageRequest pageRequest, Role role, InterestField interest) {
        List<RoleField> roleFields = Arrays.stream(RoleField.getFieldsByRole(role))
                .filter(field -> field.getRole() == role)
                .toList();

        if (roleFields.isEmpty()) {
            return List.of();
        }

        return projectRepository.findHomeProjectsByRoleAndInterest(
                userId,
                RecruitmentStatus.OPEN,
                interest,
                roleFields,
                pageRequest
        );
    }

    public List<Project> getProjects(Long userId) {
        return (userId == null)
                ? projectRepository.findHomeProjectsWithoutUser(RecruitmentStatus.OPEN)
                : projectRepository.findHomeProjects(userId, RecruitmentStatus.OPEN);
    }

    public MyProjectsResponseDto.ProjectInfo getProject(Long projectId) {
        if (projectId == null) {
            throw new ProjectException(ProjectErrorCode.INVALID_REQUEST, "projectId is required");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        List<ProjectTeamRole> teamRoles = projectTeamRoleRepository.findByProjectId(projectId);
        List<MyProjectsResponseDto.TeamRoleInfo> roleInfos = teamRoles.stream()
                .map(role -> MyProjectsResponseDto.TeamRoleInfo.builder()
                        .roleField(role.getRoleField())
                        .requiredCount(role.getRequiredCount())
                        .build())
                .toList();

        MyProjectsResponseDto.LeaderInfo leaderInfo = projectUserRepositoryComplete
                .findByProjectIdAndMemberType(projectId, ProjectMemberType.LEADER)
                .map(ProjectUser::getUserId)
                .flatMap(userRepository::findById)
                .map(leader -> MyProjectsResponseDto.LeaderInfo.builder()
                        .userId(leader.getUserId())
                        .name(leader.getName())
                        .profileImageUrl(s3Service.getPresignedGetUrl(leader.getProfileImageName()))
                        .build())
                .orElse(null);

        List<ProjectUser> activeMembers = projectUserRepositoryComplete
                .findByProjectIdAndMemberStatus(projectId, ProjectMemberStatus.ACTIVE);
        List<MyProjectsResponseDto.TeamMemberProjectInfo> teamMemberProjects =
                getTeamMemberProjectsByProject(activeMembers, projectId);

        return MyProjectsResponseDto.ProjectInfo.builder()
                .projectId(projectId)
                .projectTitle(project.getTitle())
                .description(project.getDescription())
                .imageName(s3Service.getPresignedGetUrl(project.getImageName()))
                .plannedStartedOn(project.getPlannedStartedOn())
                .plannedEndedOn(project.getPlannedEndedOn())
                .teamRoles(roleInfos)
                .leader(leaderInfo)
                .teamMemberProjects(teamMemberProjects)
                .build();
    }

    public Integer getDDay(Project project) {
        LocalDateTime endedAt = project.getEndedAt();
        LocalDate today = LocalDate.now();
        LocalDate endDate = endedAt.toLocalDate();
        return (int) ChronoUnit.DAYS.between(today, endDate);
    }

    private List<MyProjectsResponseDto.TeamMemberProjectInfo> getTeamMemberProjectsByProject(List<ProjectUser> activeMembers, Long projectId) {

        List<Long> teamMemberIds = activeMembers.stream()
                .map(ProjectUser::getUserId)
                .distinct()
                .toList();

        if (teamMemberIds.isEmpty()) {
            return List.of();
        }

        List<ProjectUser> teamMemberProjects = projectUserRepositoryComplete
                .findByUserIdInAndMemberStatus(teamMemberIds, ProjectMemberStatus.ACTIVE);

        return teamMemberProjects.stream()
                .map(ProjectUser::getProject)
                .filter(project -> !project.getId().equals(projectId))
                .distinct()
                .map(project -> MyProjectsResponseDto.TeamMemberProjectInfo.builder()
                        .projectId(project.getId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .imageName(s3Service.getPresignedGetUrl(project.getImageName()))
                        .createdAt(project.getCreatedAt())
                        .endedAt(project.getEndedAt())
                        .build())
                .toList();
    }
}
