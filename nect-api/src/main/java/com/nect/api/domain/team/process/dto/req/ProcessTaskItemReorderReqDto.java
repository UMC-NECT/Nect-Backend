package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

import java.util.List;

public record ProcessTaskItemReorderReqDto(
        @JsonProperty("ordered_task_item_ids")
        List<Long> orderedTaskItemIds,

        @JsonProperty("role_field")
        RoleField roleField,

        @JsonProperty("custom_role_field_name")
        String customRoleFieldName

) {}
