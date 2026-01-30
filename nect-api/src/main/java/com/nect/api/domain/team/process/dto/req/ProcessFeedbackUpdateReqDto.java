package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessFeedbackUpdateReqDto(
        @JsonProperty("content")
        String content
) {}
