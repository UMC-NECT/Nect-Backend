package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ScheduleCreateResDto(
        @JsonProperty("schedule_id")
        Long scheduleId
) {}