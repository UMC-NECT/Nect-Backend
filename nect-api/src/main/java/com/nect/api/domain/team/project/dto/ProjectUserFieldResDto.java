package com.nect.api.domain.team.project.dto;

import com.nect.core.entity.user.enums.RoleField;
import lombok.Builder;

@Builder
public record ProjectUserFieldResDto(
        Long projectUserId,
        RoleField field,
        String customField
) {
}
