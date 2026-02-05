package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.converter.ProjectUserConverter;
import com.nect.api.domain.team.project.dto.ProjectUserFieldReqDto;
import com.nect.api.domain.team.project.dto.ProjectUserFieldResDto;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.domain.team.project.dto.UserProjectDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.enums.code.ProjectUserErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.domain.team.project.exception.ProjectUserException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

    private final ProjectUserRepository projectUserRepository;

    public ProjectUser addProjectUser(Long userId, Project project, RoleField field){
        ProjectUser projectUser = ProjectUser.builder()
                .project(project)
                .userId(userId)
                .roleField(field)
                .build();

        projectUserRepository.save(projectUser);
        return projectUser;
    }

    @Transactional
    public ProjectUserFieldResDto changeProjectUserFieldInProject(Long projectUserId, ProjectUserFieldReqDto reqDto) {
        ProjectUser projectUser = getProjectUser(projectUserId);

        if (reqDto.field() == RoleField.CUSTOM){
            if (reqDto.customField() == null || reqDto.customField().isBlank()) {
                throw new ProjectUserException(ProjectUserErrorCode.CUSTOM_FIELD_REQUIRED);
            }
        }

        projectUser.changeField(reqDto.field(), reqDto.customField());
        return ProjectUserFieldResDto.builder()
                .projectUserId(projectUser.getId())
                .field(projectUser.getRoleField())
                .customField(projectUser.getCustomRoleFieldName())
                .build();
    }

    private ProjectUser getProjectUser(Long projectUserId) {
        ProjectUser projectUser = projectUserRepository.findById(projectUserId)
                .orElseThrow(() -> new ProjectUserException(ProjectUserErrorCode.PROJECT_USER_NOT_FOUND));
        return projectUser;
    }

    public void validateLeader(Project project, User user){
        ProjectUser projectUser = projectUserRepository.findByUserIdAndProject(user.getUserId(), project)
                .orElseThrow(
                        () -> new ProjectException(ProjectErrorCode.PROJECT_USER_NOT_FOUND)
                );

        if (projectUser.getMemberType() != ProjectMemberType.LEADER){
            throw new ProjectException(ProjectErrorCode.LEADER_ONLY_ACTION);
        }
    }

    public List<UserProjectDto> findProjectsByUser(Long userId) {
        List<ProjectUser> projectUsers = projectUserRepository.findByUserIdAndProjectMemberStatus(
                userId,
                ProjectMemberStatus.ACTIVE
        );

        return projectUsers.stream().
                map(ProjectUserConverter::toUserProjectDto)
                .toList();
    }

    public ProjectUserResDto kickProjectUser(Long userId, Long projectUserId) {
        ProjectUser projectUser = getProjectUser(projectUserId);

        // 로그인한 유저가 리더인지 검증
        if (userId != projectUserRepository.findLeaderByProject(projectUser.getProject())){
            throw new ProjectUserException(ProjectUserErrorCode.ONLY_LEADER_ALLOWED);
        }

        projectUser.kick();
        return ProjectUserConverter.toProjectUserResDto(projectUser);
    }

    public ProjectUserResDto changeProjectUserTypeInProject(Long userId, Long projectUserId, ProjectMemberType memberType) {
        ProjectUser projectUser = getProjectUser(projectUserId);

        // 로그인한 유저가 리더인지 검증
        if (userId != projectUserRepository.findLeaderByProject(projectUser.getProject())){
            throw new ProjectUserException(ProjectUserErrorCode.ONLY_LEADER_ALLOWED);
        }

        // 한 프로젝트, 같은 분야에서 LEAD는 한명만 설정 가능하도록 검증
        if (memberType == ProjectMemberType.LEAD) {
            RoleField roleField = projectUser.getRoleField();
            boolean anotherLeadExists = projectUserRepository.existsActiveLeadInProject(
                    projectUser.getProject(),
                    roleField,
                    projectUser.getCustomRoleFieldName()
            );

            if (anotherLeadExists && projectUser.getMemberType() != ProjectMemberType.LEADER) {
                throw new ProjectUserException(ProjectUserErrorCode.ONLY_LEADER_ALLOWED);
            }
        }

        projectUser.changeType(memberType);
        return ProjectUserConverter.toProjectUserResDto(projectUser);
    }
}
