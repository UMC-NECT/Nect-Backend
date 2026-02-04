package com.nect.api.domain.team.project.dto;

import com.nect.core.entity.user.enums.RoleField;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProjectUserFieldReqDto(
        @NotNull RoleField field,
        String customField
) {
}
