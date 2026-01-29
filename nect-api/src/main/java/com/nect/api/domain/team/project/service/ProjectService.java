package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.Project;
import com.nect.core.repository.team.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public Project getProject(Long projectId){
        return projectRepository.findById(projectId)
                .orElseThrow(
                        () -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND)
                );
    }
}
