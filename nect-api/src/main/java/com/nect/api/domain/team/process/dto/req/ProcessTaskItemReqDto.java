package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessTaskItemReqDto(
        String content,

        @JsonProperty("is_done")
        Boolean isDone,

        @JsonProperty("sort_order")
        Integer sortOrder
) {}
