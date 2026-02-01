package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProcessTaskItemReorderReqDto(
        @JsonProperty("ordered_task_item_ids")
        List<Long> orderedTaskItemIds
) {}
