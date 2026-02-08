package com.nect.api.domain.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserTeamRoleUpdateReqDto(
        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("required_count")
        Integer requiredCount
) {}
