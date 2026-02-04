package com.nect.api.domain.team.project.dto;

import com.nect.core.entity.team.enums.ProjectMemberType;
import lombok.Builder;

@Builder
public record UserProjectDto(
        Long projectId,
        ProjectMemberType memberType
) {
}
