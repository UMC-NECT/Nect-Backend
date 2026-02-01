package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleUpcomingResDto(
        @JsonProperty("items")
        List<Item> items
) {
    public record Item(
            @JsonProperty("schedule_id")
            Long scheduleId,

            @JsonProperty("title")
            String title,

            @JsonProperty("start_at")
            LocalDateTime startAt,

            @JsonProperty("end_at")
            LocalDateTime endAt,

            @JsonProperty("all_day")
            boolean allDay,

            @JsonProperty("is_multi_day")
            boolean isMultiDay
    ) {}
}
