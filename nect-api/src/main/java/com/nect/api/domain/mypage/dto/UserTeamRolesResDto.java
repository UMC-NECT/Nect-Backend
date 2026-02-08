package com.nect.api.domain.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

import java.util.List;

public record UserTeamRolesResDto(
        @JsonProperty("parts")
        List<PartDto> parts
) {
    public record PartDto(
            @JsonProperty("id")
            Long id,

            @JsonProperty("role_field")
            RoleField roleField,

            @JsonProperty("custom_role_field_name")
            String customRoleFieldName,

            @JsonProperty("label")
            String label,

            @JsonProperty("required_count")
            Integer requiredCount
    ) {}
}
