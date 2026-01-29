package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProcessTaskItemReorderResDto(
        @JsonProperty("process_id")
        Long processId,

        @JsonProperty("ordered_task_items")
        List<ProcessTaskItemResDto> orderedTaskItems
) {}
