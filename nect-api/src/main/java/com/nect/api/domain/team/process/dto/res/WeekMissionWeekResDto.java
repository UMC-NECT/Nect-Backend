package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.time.LocalDate;
import java.util.List;

public record WeekMissionWeekResDto(
        @JsonProperty("week_start")
        LocalDate weekStart,

        @JsonProperty("week_end")
        LocalDate weekEnd,

        @JsonProperty("missions")
        List<WeekMissionCardResDto> missions
) {
    public record WeekMissionCardResDto(
            @JsonProperty("process_id")
            Long processId,

            @JsonProperty("mission_number")
            Integer missionNumber,

            @JsonProperty("status")
            ProcessStatus status,

            @JsonProperty("title")
            String title,

            @JsonProperty("start_date")
            LocalDate startDate,

            @JsonProperty("dead_line")
            LocalDate deadLine,

            @JsonProperty("left_day")
            Integer leftDay,

            @JsonProperty("done_count")
            int doneCount,

            @JsonProperty("total_count")
            int totalCount,

            @JsonProperty("assignee")
            AssigneeProfileDto assignee
    ) {}

    public record AssigneeProfileDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {}
}
