package com.nect.api.domain.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record UserTeamRoleCreateResDto(
        @JsonProperty("team_role_id")
        Long teamRoleId,

        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName,

        @JsonProperty("part_label")
        String partLabel,

        @JsonProperty("required_count")
        Integer requiredCount
) {}
