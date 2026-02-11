package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.converter.ProjectListConverter;
import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.team.project.dto.ProjectMemberStatisticResponse;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.exception.CustomException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectInterest;
import com.nect.core.entity.team.ProjectPlanFile;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.PlanFileType;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;

import com.nect.core.repository.team.ProjectInterestFieldRepository;
import com.nect.core.repository.team.ProjectPlanFileRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.user.ProjectUserRepositoryComplete;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserTeamRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// 마이페이지-프로젝트 데이터 조회(query) service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageProjectQueryService {

    private final ProjectUserRepositoryComplete projectUserRepositoryComplete;
    private final UserRepository userRepository;
    private final ProjectInterestFieldRepository projectInterestFieldRepository;
    private final ProjectPlanFileRepository projectPlanFileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectPlanFileRepository planFileRepository;
    private final ProjectListConverter projectListConverter;
    private final S3Service s3Service;
    private final UserTeamRoleRepository userTeamRoleRepository;

    public MyProjectsResponseDto getMyProjects(Long userId) {
        List<ProjectUser> myProjectUsers = projectUserRepositoryComplete
                .findByUserIdAndMemberStatus(userId, ProjectMemberStatus.ACTIVE);

        if (myProjectUsers.isEmpty()) {
            return MyProjectsResponseDto.builder()
                    .projects(List.of())
                    .build();
        }

        List<Long> projectIds = myProjectUsers.stream()
                .map(pu -> pu.getProject().getId())
                .collect(Collectors.toList());

        Map<Long, MyProjectsResponseDto.LeaderInfo> leadersMap = getLeadersMapByProjects(projectIds);

        Map<Long, List<MyProjectsResponseDto.TeamMemberProjectInfo>> teamMemberProjectsMap =
                getTeamMemberProjectsMapByProjects(projectIds, userId);

        Map<Long, List<UserTeamRole>> teamRolesByProjectId = userTeamRoleRepository
                .findByProjectIdIn(projectIds).stream()
                .collect(Collectors.groupingBy(tr -> tr.getProject().getId()));


        List<MyProjectsResponseDto.ProjectInfo> projectInfos = myProjectUsers.stream()
                .map(projectUser -> {
                    Project project = projectUser.getProject();
                    Long projectId = project.getId();


                    ProjectMemberStatisticResponse teamRoles = buildMemberStatistics(
                            teamRolesByProjectId.getOrDefault(projectId, List.of())
                    );

                    return MyProjectsResponseDto.ProjectInfo.builder()
                            .projectId(projectId)
                            .projectTitle(project.getTitle())
                            .description(project.getDescription())
                            .imageName(project.getImageName())
                            .plannedStartedOn(project.getPlannedStartedOn())
                            .plannedEndedOn(project.getPlannedEndedOn())
                            .teamRoles(teamRoles)
                            .leader(leadersMap.get(projectId))
                            .teamMemberProjects(teamMemberProjectsMap.getOrDefault(projectId, List.of()))
                            .recruitmentStatus(project.getRecruitmentStatus())
                            .build();
                })
                .collect(Collectors.toList());

        return MyProjectsResponseDto.builder()
                .projects(projectInfos)
                .build();
    }


    private ProjectMemberStatisticResponse buildMemberStatistics(List<UserTeamRole> teamRoles) {
        Map<RoleField, Integer> roleFieldCounts = teamRoles.stream()
                .filter(tr -> !tr.isDeleted())
                .collect(Collectors.groupingBy(
                        UserTeamRole::getRoleField,
                        Collectors.summingInt(UserTeamRole::getRequiredCount)
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
                    List<ProjectMemberStatisticResponse.RoleFieldStatistic> roleFields = Arrays.stream(RoleField.values())
                            .filter(rf -> {
                                Role rfRole = rf.getRole();
                                return ((rfRole == null) ? Role.OTHER : rfRole) == role;
                            })
                            .filter(rf -> roleFieldCounts.containsKey(rf))
                            .map(rf -> new ProjectMemberStatisticResponse.RoleFieldStatistic(
                                    rf,
                                    roleFieldCounts.get(rf)
                            ))
                            .toList();

                    int totalCount = roleFields.stream()
                            .mapToInt(ProjectMemberStatisticResponse.RoleFieldStatistic::count)
                            .sum();

                    return new ProjectMemberStatisticResponse.RoleStatistic(
                            role,
                            totalCount,
                            roleFields
                    );
                })
                .filter(rs -> rs.count() > 0)
                .toList();

        return new ProjectMemberStatisticResponse(roles);
    }

    public MyProjectsResponseDto.ProjectFieldResponse getProjectFields(Long projectId) {
        List<ProjectInterest> projectInterests = projectInterestFieldRepository.findByProjectId(projectId);
        return MyProjectsResponseDto.ProjectFieldResponse.ofProject(projectId, projectInterests);
    }

    public MyProjectsResponseDto.StringListResponse getPurposes(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));
        String purpose = project.getPurposes();
        List<String> slicedPurpose = projectListConverter.convertToEntityAttribute(purpose);
        return new MyProjectsResponseDto.StringListResponse(projectId, slicedPurpose);
    }

    public MyProjectsResponseDto.StringListResponse getFunctions(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));
        String functions = project.getMainFunctions();
        List<String> slicedPurpose = projectListConverter.convertToEntityAttribute(functions);
        return new MyProjectsResponseDto.StringListResponse(projectId, slicedPurpose);
    }

    public MyProjectsResponseDto.StringListResponse getServiceUsers(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));
        String serviceUsers = project.getServiceUsers();
        List<String> slicedUsers = projectListConverter.convertToEntityAttribute(serviceUsers);
        return new MyProjectsResponseDto.StringListResponse(projectId, slicedUsers);
    }

    public MyProjectsResponseDto.ProjectPlanFilesResponse getPlanFiles(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        List<ProjectPlanFile> files = projectPlanFileRepository.findByProjectId(projectId);
        List<MyProjectsResponseDto.ProjectPlanFileInfo> infos = files.stream()
                .map(file -> new MyProjectsResponseDto.ProjectPlanFileInfo(
                        file.getId(),
                        file.getName(),
                        file.getFileName(),
                        file.getPlanFileType(),
                        file.getFileExt()
                ))
                .toList();

        return new MyProjectsResponseDto.ProjectPlanFilesResponse(project.getId(), infos);
    }

    @Transactional
    public String getPlanFileDownloadUrl(Long projectId, Long planFileId) {
        if (projectId == null || planFileId == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }

        ProjectPlanFile planFile = planFileRepository.findByIdAndProjectId(planFileId, projectId)
                .orElseThrow(() -> new CustomException(CommonResponseCode.NOT_FOUND_ERROR));

        if (planFile.getPlanFileType() != PlanFileType.FILE) {
            throw new CustomException(CommonResponseCode.BAD_REQUEST_ERROR);
        }

        String fileKey = planFile.getFileName();
        if (fileKey == null || fileKey.isBlank()) {
            throw new CustomException(CommonResponseCode.NOT_FOUND_ERROR);
        }

        return s3Service.getPresignedGetUrl(fileKey);
    }


    private Map<Long, MyProjectsResponseDto.LeaderInfo> getLeadersMapByProjects(List<Long> projectIds) {

        List<ProjectUser> leaderProjectUsers = projectUserRepositoryComplete
                .findByProjectIdInAndMemberType(projectIds, ProjectMemberType.LEADER);

        if (leaderProjectUsers.isEmpty()) {
            return Map.of();
        }

        List<Long> leaderUserIds = leaderProjectUsers.stream()
                .map(ProjectUser::getUserId)
                .collect(Collectors.toList());

        Map<Long, User> usersMap = userRepository.findAllById(leaderUserIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        return leaderProjectUsers.stream()
                .collect(Collectors.toMap(
                        pu -> pu.getProject().getId(),
                        pu -> {
                            User leader = usersMap.get(pu.getUserId());
                            if (leader == null) {
                                return null;
                            }
                            return MyProjectsResponseDto.LeaderInfo.builder()
                                    .userId(leader.getUserId())
                                    .name(leader.getName())
                                    .profileImageUrl(leader.getProfileImageName())
                                    .build();
                        }
                ));
    }


    private Map<Long, List<MyProjectsResponseDto.TeamMemberProjectInfo>> getTeamMemberProjectsMapByProjects(
            List<Long> projectIds, Long currentUserId) {


        List<ProjectUser> myAllProjects = projectUserRepositoryComplete
                .findByUserIdAndMemberStatus(
                        currentUserId,
                        ProjectMemberStatus.ACTIVE);

        if (myAllProjects.isEmpty()) {
            return Map.of();
        }

        List<MyProjectsResponseDto.TeamMemberProjectInfo> allProjectInfos = myAllProjects.stream()
                .map(pu -> {
                    Project project = pu.getProject();
                    return MyProjectsResponseDto.TeamMemberProjectInfo.builder()
                            .projectId(project.getId())
                            .title(project.getTitle())
                            .description(project.getDescription())
                            .imageName(project.getImageName())
                            .createdAt(project.getCreatedAt())
                            .endedAt(project.getEndedAt())
                            .build();
                })
                .collect(Collectors.toList());


        return projectIds.stream()
                .collect(Collectors.toMap(
                        projectId -> projectId,
                        projectId -> allProjectInfos.stream()
                                .filter(info -> !info.getProjectId().equals(projectId))
                                .collect(Collectors.toList())
                ));
    }



}
