package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record BoardsBasicInfoGetResDto(
        @JsonProperty("project_id")
        Long projectId,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("notice_text")
        String noticeText,

        @JsonProperty("regular_meeting_text")
        String regularMeetingText,

        @JsonProperty("planned_started_on")
        LocalDate plannedStartedOn,

        @JsonProperty("planned_ended_on")
        LocalDate plannedEndedOn,

        @JsonProperty("remaining_days")
        long remainingDays,

        @JsonProperty("can_edit")
        boolean canEdit
) {}