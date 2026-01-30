package com.nect.api.domain.team.history.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProjectHistoryListResDto(
        @JsonProperty("next_cursor")
        Long nextCursor,

        @JsonProperty("items")
        List<ProjectHistoryResDto> items
) {
}
