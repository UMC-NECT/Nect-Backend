package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.converter.ProjectUserConverter;
import com.nect.api.domain.team.project.dto.UserProjectDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

    private final ProjectUserRepository projectUserRepository;

    public ProjectUser addProjectUser(Long userId, Project project, RoleField fieldId){
        ProjectUser projectUser = ProjectUser.builder()
                .project(project)
                .userId(userId)
//                .fieldId(fieldId)
                .build();

        projectUserRepository.save(projectUser);
        return projectUser;
    }

    public void validateLeader(Project project, Long userId){
        ProjectUser projectUser = projectUserRepository.findByUserIdAndProject(userId, project)
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
}
