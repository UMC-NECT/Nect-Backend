package com.nect.api.domain.home.service;

import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.team.project.dto.ProjectMemberStatisticResponse;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
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
import com.nect.core.repository.user.ProjectUserRepositoryComplete;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserTeamRoleRepository;
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
    private final UserTeamRoleRepository userTeamRoleRepository;
    private final S3Service s3Service;

    public record HomeProjectBatch(
            Map<Long, User> authorByProjectId,
            Map<Long, Integer> activeCountByProjectId,
            Map<Long, Integer> maxMemberCountByProjectId,
            Map<Long, ProjectMemberStatisticResponse> memberStatisticsByProjectId
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

        Map<Long, Integer> maxMemberCountByProjectId = userTeamRoleRepository.sumRequirementByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        UserTeamRoleRepository.ProjectRequirementRow::getProjectId,
                        utr -> utr.getRequirementSum() == null ? 0 : utr.getRequirementSum()
                ));

        Map<Long, List<ProjectUser>> projectUsersByProjectId = projectUserRepositoryComplete
                .findByProjectIdInAndMemberStatus(projectIds, ProjectMemberStatus.ACTIVE).stream()
                .collect(Collectors.groupingBy(pu -> pu.getProject().getId()));

        Map<Long, ProjectMemberStatisticResponse> memberStatisticsByProjectId = new HashMap<>();
        for (Long projectId : projectIds) {
            List<ProjectUser> members = projectUsersByProjectId.getOrDefault(projectId, List.of());
            memberStatisticsByProjectId.put(projectId, buildMemberStatistics(members));
        }

        return new HomeProjectBatch(
                authorByProjectId,
                activeCountByProjectId,
                maxMemberCountByProjectId,
                memberStatisticsByProjectId
        );
    }

    private ProjectMemberStatisticResponse buildMemberStatistics(List<ProjectUser> members) {
        Map<Role, List<ProjectUser>> byRole = members.stream()
                .collect(Collectors.groupingBy(
                        pu -> {
                            Role role = pu.getRoleField().getRole();
                            return (role == null) ? Role.OTHER : role;
                        },
                        () -> new EnumMap<>(Role.class),
                        Collectors.toList()
                ));

        List<Role> roleOrder = List.of(
                Role.PLANNER,
                Role.DESIGNER,
                Role.DEVELOPER,
                Role.MARKETER,
                Role.OTHER
        );

        List<ProjectMemberStatisticResponse.RoleStatistic> roles = roleOrder.stream()
                .map(role -> {
                    List<ProjectUser> roleUsers = byRole.getOrDefault(role, List.of());
                    Map<RoleField, Long> roleFieldCounts = roleUsers.stream()
                            .collect(Collectors.groupingBy(ProjectUser::getRoleField, Collectors.counting()));

                    List<ProjectMemberStatisticResponse.RoleFieldStatistic> roleFields = Arrays.stream(RoleField.values())
                            .filter(rf -> {
                                Role rfRole = rf.getRole();
                                return ((rfRole == null) ? Role.OTHER : rfRole) == role;
                            })
                            .map(rf -> {
                                Long count = roleFieldCounts.get(rf);
                                if (count == null || count == 0) return null;
                                return new ProjectMemberStatisticResponse.RoleFieldStatistic(rf, count.intValue());
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    return new ProjectMemberStatisticResponse.RoleStatistic(
                            role,
                            roleUsers.size(),
                            roleFields
                    );
                })
                .toList();

        return new ProjectMemberStatisticResponse(roles);
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

        ProjectMemberStatisticResponse memberStatistics = buildMemberStatistics(
                projectUserRepositoryComplete.findByProjectIdAndMemberStatus(projectId, ProjectMemberStatus.ACTIVE)
        );

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
                .teamRoles(memberStatistics)
                .leader(leaderInfo)
                .teamMemberProjects(teamMemberProjects)
                .build();
    }

    public Integer getDDay(Project project) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = project.getPlannedEndedOn();
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
                        .endedAt(project.getPlannedEndedOn().atStartOfDay())
                        .build())
                .toList();
    }
}
