package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void validateLeader(Project project, User user){
        ProjectUser projectUser = projectUserRepository.findByUserIdAndProject(user.getUserId(), project)
                .orElseThrow(
                        () -> new ProjectException(ProjectErrorCode.PROJECT_USER_NOT_FOUND)
                );

        if (projectUser.getMemberType() != ProjectMemberType.LEADER){
            throw new ProjectException(ProjectErrorCode.LEADER_ONLY_ACTION);
        }
    }
}
