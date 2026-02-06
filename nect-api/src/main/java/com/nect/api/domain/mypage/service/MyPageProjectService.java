package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.team.process.ProjectTeamRole;
import com.nect.core.entity.user.User;

import com.nect.core.repository.analysis.ProjectTeamRoleRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.user.ProjectUserRepositoryComplete;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageProjectService {

    private final ProjectUserRepositoryComplete projectUserRepositoryComplete;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectTeamRoleRepository projectTeamRoleRepository;


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

        Map<Long, List<ProjectTeamRole>> teamRolesMap = getTeamRolesMapByProjects(projectIds);

        Map<Long, MyProjectsResponseDto.LeaderInfo> leadersMap = getLeadersMapByProjects(projectIds);

        Map<Long, List<MyProjectsResponseDto.TeamMemberProjectInfo>> teamMemberProjectsMap =
                getTeamMemberProjectsMapByProjects(projectIds, userId);

        List<MyProjectsResponseDto.ProjectInfo> projectInfos = myProjectUsers.stream()
                .map(projectUser -> {
                    Project project = projectUser.getProject();
                    Long projectId = project.getId();

                    List<MyProjectsResponseDto.TeamRoleInfo> teamRoles = teamRolesMap
                            .getOrDefault(projectId, List.of())
                            .stream()
                            .map(role -> MyProjectsResponseDto.TeamRoleInfo.builder()
                                    .roleField(role.getRoleField())
                                    .requiredCount(role.getRequiredCount())
                                    .build())
                            .collect(Collectors.toList());

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
                            .build();
                })
                .collect(Collectors.toList());

        return MyProjectsResponseDto.builder()
                .projects(projectInfos)
                .build();
    }


    private Map<Long, List<ProjectTeamRole>> getTeamRolesMapByProjects(List<Long> projectIds) {
        List<ProjectTeamRole> allTeamRoles = projectTeamRoleRepository
                .findByProjectIdIn(projectIds);

        return allTeamRoles.stream()
                .collect(Collectors.groupingBy(role -> role.getProject().getId()));
    }


    private Map<Long, MyProjectsResponseDto.LeaderInfo> getLeadersMapByProjects(List<Long> projectIds) {
        // 모든 프로젝트의 리더 조회
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
                                    .profileImageUrl(leader.getProfileImageUrl())
                                    .build();
                        }
                ));
    }


    private Map<Long, List<MyProjectsResponseDto.TeamMemberProjectInfo>> getTeamMemberProjectsMapByProjects(
            List<Long> projectIds, Long currentUserId) {

        List<ProjectUser> allTeamMembers = projectUserRepositoryComplete
                .findByProjectIdInAndMemberStatus(projectIds, ProjectMemberStatus.ACTIVE)
                .stream()
                .filter(pu -> !pu.getUserId().equals(currentUserId))
                .collect(Collectors.toList());

        if (allTeamMembers.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Long>> projectTeamMembersMap = allTeamMembers.stream()
                .collect(Collectors.groupingBy(
                        pu -> pu.getProject().getId(),
                        Collectors.mapping(ProjectUser::getUserId, Collectors.toList())
                ));

        List<Long> allTeamMemberUserIds = allTeamMembers.stream()
                .map(ProjectUser::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<ProjectUser> teamMemberProjects = projectUserRepositoryComplete
                .findByUserIdInAndMemberStatus(
                        allTeamMemberUserIds,
                        ProjectMemberStatus.ACTIVE);

        Map<Long, List<Project>> userProjectsMap = teamMemberProjects.stream()
                .filter(pu -> !projectIds.contains(pu.getProject().getId()))
                .collect(Collectors.groupingBy(
                        ProjectUser::getUserId,
                        Collectors.mapping(ProjectUser::getProject, Collectors.toList())
                ));

        return projectTeamMembersMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<Long> teamMemberIds = entry.getValue();

                            return teamMemberIds.stream()
                                    .flatMap(userId -> userProjectsMap.getOrDefault(userId, List.of()).stream())
                                    .distinct()
                                    .map(project -> MyProjectsResponseDto.TeamMemberProjectInfo.builder()
                                            .projectId(project.getId())
                                            .title(project.getTitle())
                                            .description(project.getDescription())
                                            .imageName(project.getImageName())
                                            .createdAt(project.getCreatedAt())
                                            .endedAt(project.getEndedAt())
                                            .build())
                                    .collect(Collectors.toList());
                        }
                ));
    }
}