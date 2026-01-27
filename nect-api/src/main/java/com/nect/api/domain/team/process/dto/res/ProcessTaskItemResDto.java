package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ProcessTaskItemResDto(
        @JsonProperty("task_item_id")
        Long taskItemId,

        String content,

        @JsonProperty("is_done")
        boolean isDone,

        @JsonProperty("sort_order")
        Integer sortOrder,

        @JsonProperty("done_at")
        LocalDate doneAt
) {
}
