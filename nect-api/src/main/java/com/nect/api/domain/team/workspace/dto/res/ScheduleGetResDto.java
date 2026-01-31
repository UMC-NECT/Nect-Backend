package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ScheduleGetResDto(

        @JsonProperty("schedule_id")
        Long scheduleId,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("start_at")
        LocalDateTime startAt,

        @JsonProperty("end_at")
        LocalDateTime endAt,

        @JsonProperty("all_day")
        boolean allDay,

        @JsonProperty("is_multi_day")
        boolean isMultiDay,

        @JsonProperty("creator_user_id")
        Long creatorUserId

) {}
