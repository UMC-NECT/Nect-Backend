package com.nect.api.domain.team.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record ProjectPartUpdateResDto(
        @JsonProperty("part_id")
        Long partId,

        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("part_label")
        String partLabel,

        @JsonProperty("required_count")
        Integer requiredCount
) {
}

