package com.nect.api.domain.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.entity.user.enums.RoleField;
import lombok.Builder;

@Builder
public record TeamRoleResponseDto(
        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("required_count")
        Integer requiredCount
) {
    public static TeamRoleResponseDto from(UserTeamRole role) {
        return TeamRoleResponseDto.builder()
                .roleField(role.getRoleField())
                .customRoleFieldName(
                        role.getRoleField() == RoleField.CUSTOM ? role.getCustomRoleFieldName() : null
                )
                .requiredCount(role.getRequiredCount())
                .build();
    }
}