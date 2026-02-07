package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record WeekMissionTaskItemUpdateReqDto(
        @JsonProperty("content")
        String content,

        @JsonProperty("is_done")
        Boolean isDone,

        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName
) {}
