package com.nect.api.domain.team.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProjectPartUpdateReqDto(
        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("required_count")
        Integer requiredCount
) {
}
