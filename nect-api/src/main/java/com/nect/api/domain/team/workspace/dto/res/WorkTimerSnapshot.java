package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record WorkTimerSnapshot(
        @JsonProperty("is_working")
        boolean isWorking,

        @JsonProperty("today_work_seconds")
        long todayWorkSeconds,

        @JsonProperty("working_started_at")
        LocalDateTime workingStartedAt
) {
    public static WorkTimerSnapshot empty() {
        return new WorkTimerSnapshot(false, 0L, null);
    }
}