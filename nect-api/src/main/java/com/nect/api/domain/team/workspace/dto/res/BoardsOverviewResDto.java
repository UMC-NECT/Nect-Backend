package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BoardsOverviewResDto(

        @JsonProperty("basic_info")
        BoardsBasicInfoGetResDto basicInfo,

        @JsonProperty("mission_progress")
        MissionProgressResDto missionProgress,

        @JsonProperty("members")
        MemberBoardResDto members,

        @JsonProperty("upcoming_schedules")
        ScheduleUpcomingResDto upcomingSchedules,

        @JsonProperty("shared_documents_preview")
        SharedDocumentsPreviewResDto sharedDocumentsPreview,

        @JsonProperty("posts_preview")
        PostsPreviewResDto postsPreview,

        @JsonProperty("calendar_month_indicators")
        CalendarMonthIndicatorsResDto calendarMonthIndicators
) {
    public static BoardsOverviewResDto of(
            BoardsBasicInfoGetResDto basicInfo,
            MissionProgressResDto missionProgress,
            MemberBoardResDto members,
            ScheduleUpcomingResDto upcomingSchedules,
            SharedDocumentsPreviewResDto sharedDocumentsPreview,
            PostsPreviewResDto postsPreview,
            CalendarMonthIndicatorsResDto calendarMonthIndicators
    ) {
        return new BoardsOverviewResDto(
                basicInfo,
                missionProgress,
                members,
                upcomingSchedules,
                sharedDocumentsPreview,
                postsPreview,
                calendarMonthIndicators
        );
    }
}