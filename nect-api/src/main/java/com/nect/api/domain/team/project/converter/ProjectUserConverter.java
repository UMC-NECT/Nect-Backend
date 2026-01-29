package com.nect.api.domain.team.project.converter;

import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.core.entity.team.ProjectUser;

public class ProjectUserConverter {

    public static ProjectUserResDto toProjectUserResDto(ProjectUser projectUser){
        return ProjectUserResDto.builder()
                .id(projectUser.getId())
                .userId(projectUser.getUserId())
                .projectId(projectUser.getProject().getId())
                .fieldId(projectUser.getFieldId())
                .memberType(projectUser.getMemberType())
                .memberStatus(projectUser.getMemberStatus())
                .build();
    }
}
