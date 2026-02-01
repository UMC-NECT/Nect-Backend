package com.nect.api.domain.team.workspace.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BoardsBasicInfoUpdateReqDto(
        @JsonProperty("notice_text")
        String noticeText,

        @JsonProperty("regular_meeting_text")
        String regularMeetingText
) {}
