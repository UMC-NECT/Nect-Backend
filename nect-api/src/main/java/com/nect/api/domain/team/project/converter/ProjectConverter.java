package com.nect.api.domain.team.project.converter;

import com.nect.api.domain.team.project.dto.RecruitingProjectResDto;
import com.nect.core.entity.team.Project;

public class ProjectConverter {

    public static RecruitingProjectResDto toRecruitingProjectResDto(Project project){
        return RecruitingProjectResDto.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .build();
    }
}
