package com.nect.api.domain.mypage.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record TeamRoleAddRequestDto(
        @JsonProperty("role_field")
        RoleField roleField,
        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,
        @JsonProperty("count")
        Integer count
) {
}