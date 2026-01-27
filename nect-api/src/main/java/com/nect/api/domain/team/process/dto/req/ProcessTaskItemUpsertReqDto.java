package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessTaskItemUpsertReqDto(
        String content,

        @JsonProperty("is_done")
        Boolean isDone,

        @JsonProperty("sort_order")
        Integer sortOrder
) {
    public ProcessTaskItemUpsertReqDto {
        isDone = (isDone == null) ? false : isDone;
    }
}
