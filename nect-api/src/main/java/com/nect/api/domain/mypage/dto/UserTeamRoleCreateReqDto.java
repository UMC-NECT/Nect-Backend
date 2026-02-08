package com.nect.api.domain.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record UserTeamRoleCreateReqDto(
        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("required_count")
        Integer requiredCount
) {
}
