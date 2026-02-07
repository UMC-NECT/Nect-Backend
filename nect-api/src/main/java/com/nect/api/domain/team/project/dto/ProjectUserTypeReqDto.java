package com.nect.api.domain.team.project.dto;

import com.nect.core.entity.team.enums.ProjectMemberType;
import jakarta.validation.constraints.NotNull;

public record ProjectUserTypeReqDto(
        @NotNull
        ProjectMemberType memberType
) {
}
