package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.dto.TeamRoleAddRequestDto;
import com.nect.api.domain.mypage.dto.TeamRoleResponseDto;
import com.nect.api.domain.mypage.dto.UserTeamRolesResDto;
import com.nect.api.domain.mypage.enums.UserTeamRoleErrorCode;
import com.nect.api.domain.mypage.exception.UserTeamRoleException;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.domain.team.project.service.ProjectMemberQueryService;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserTeamRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTeamRoleQueryService {

    private final UserTeamRoleRepository userTeamRoleRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ProjectUserService projectUserService;
    private final ProjectMemberQueryService projectMemberQueryService;

    // 마이페이지 팀 파트 조회(UserTeamRole 사용), 프로젝트 멤버면 조회 가능
    @Transactional(readOnly = true)
    public UserTeamRolesResDto readMyPageParts(Long projectId, Long requesterUserId) {

        if (projectId == null) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST, "projectId is required");
        }

        boolean isActiveMember = projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(
                projectId, requesterUserId, ProjectMemberStatus.ACTIVE
        );
        if (!isActiveMember) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.FORBIDDEN_NOT_PROJECT_MEMBER,
                    "projectId=" + projectId + ", userId=" + requesterUserId);
        }

        List<UserTeamRole> roles =
                userTeamRoleRepository.findAllByProject_IdAndDeletedAtIsNullOrderByIdAsc(projectId);

        List<UserTeamRolesResDto.PartDto> parts = roles.stream()
                .map(r -> {

                    List<ProjectUser> projectUsers = projectUserRepository.findByProjectIdAndRoleField(r.getProject().getId(), r.getRoleField());
                    List<Long> userIds = projectUsers.stream()
                            .map(ProjectUser::getUserId)
                            .toList();

                    List<User> users = userIds.stream()
                            .map(userRepository::findById)   // Optional<User>
                            .flatMap(Optional::stream)       // 존재하는 것만 User로 변환
                            .toList();

                    List<UserTeamRolesResDto.PartUserInfo> userInfos = users.stream()
                            .map(u -> new UserTeamRolesResDto.PartUserInfo(
                                    u.getUserId(),
                                    s3Service.getPresignedGetUrl(u.getProfileImageName()),
                                    u.getNickname(),
                                    projectMemberQueryService.roleFieldInProject(projectId, u.getUserId()).getLabelEn()
                            )).toList();

                    return new UserTeamRolesResDto.PartDto(
                        r.getId(),
                        r.getRoleField(),
                        r.getCustomRoleFieldName(),
                        r.getLabel(),
                        r.getRequiredCount(),
                        userInfos
                    );

                })
                .toList();

        return new UserTeamRolesResDto(parts);
    }

    @Transactional
    public void addTeamRole(Long userId, Long projectId, TeamRoleAddRequestDto request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        validateProjectLeader(projectId, userId);


        UserTeamRole existingRole = userTeamRoleRepository
                .findFirstByProjectIdAndRoleFieldAndCustomRoleFieldName(
                        projectId,
                        request.roleField(),
                        (request.roleField() == RoleField.CUSTOM) ? request.customRoleFieldName() : null
                )
                .orElse(null);


        if (existingRole != null) {
            existingRole.updateRequiredCount(request.count());
        } else {
            UserTeamRole newRole = UserTeamRole.builder()
                    .project(project)
                    .roleField(request.roleField())
                    .customRoleFieldName(
                            request.roleField() == RoleField.CUSTOM ? request.customRoleFieldName() : null
                    )
                    .requiredCount(request.count())
                    .build();

            userTeamRoleRepository.save(newRole);
        }
    }
    private void validateProjectLeader(Long projectId, Long userId) {
        boolean isLeader = projectUserRepository.existsByProjectIdAndUserIdAndMemberType(
                projectId,
                userId,
                ProjectMemberType.LEADER
        );
        if (!isLeader) {
            throw new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public List<TeamRoleResponseDto> getTeamRoles(Long projectId) {

        List<UserTeamRole> roles = userTeamRoleRepository.findAllByProjectId(projectId);
        return roles.stream()
                .map(TeamRoleResponseDto::from)
                .collect(Collectors.toList());
    }
}
