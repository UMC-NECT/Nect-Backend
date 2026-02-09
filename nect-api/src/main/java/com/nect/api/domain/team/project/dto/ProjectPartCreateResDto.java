package com.nect.api.domain.team.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record ProjectPartCreateResDto(
        @JsonProperty("part_id")
        Long partId,

        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("part_label")
        String part_label,

        @JsonProperty("required_count")
        Integer requiredCount
) {
}
