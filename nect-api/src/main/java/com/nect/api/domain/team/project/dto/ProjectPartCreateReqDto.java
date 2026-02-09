package com.nect.api.domain.team.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record ProjectPartCreateReqDto(
        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("required_count")
        Integer requiredCount
) {
}
