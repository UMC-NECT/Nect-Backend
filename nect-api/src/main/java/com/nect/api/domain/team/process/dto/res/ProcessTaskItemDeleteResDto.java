package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessTaskItemDeleteResDto(
        @JsonProperty("task_item_id")
        Long taskItemId
) {
}
