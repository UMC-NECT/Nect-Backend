package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.domain.user.service.UserService;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserService userService;

    public Project getProject(Long projectId){
        return projectRepository.findById(projectId)
                .orElseThrow(
                        () -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND)
                );
    }

    public User getLeader(Project project){
        Long userId = projectUserRepository.findLeaderByProject(project);

        return userService.getUser(userId);
    }

    public List<Project> getProjectsAsLeader(Long userId){
        return projectUserRepository.findProjectsAsLeader(userId);
    }

    public long getUserNumberOfProject(Project project){
        return projectUserRepository.countProjectUserByMemberStatusAndProject(
                ProjectMemberStatus.ACTIVE,
                project
        );
    }
}
