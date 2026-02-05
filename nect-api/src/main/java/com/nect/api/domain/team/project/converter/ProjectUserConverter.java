package com.nect.api.domain.team.project.converter;

import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.domain.team.project.dto.UserProjectDto;
import com.nect.core.entity.team.ProjectUser;

public class ProjectUserConverter {

    public static ProjectUserResDto toProjectUserResDto(ProjectUser projectUser){
        return ProjectUserResDto.builder()
                .id(projectUser.getId())
                .userId(projectUser.getUserId())
                .projectId(projectUser.getProject().getId())
                .field(projectUser.getRoleField())
                .memberType(projectUser.getMemberType())
                .memberStatus(projectUser.getMemberStatus())
                .build();
    }

    public static UserProjectDto toUserProjectDto(ProjectUser projectUser){
        return UserProjectDto.builder()
                .projectId(projectUser.getProject().getId())
                .memberType(projectUser.getMemberType())
                .build();
    }
}
