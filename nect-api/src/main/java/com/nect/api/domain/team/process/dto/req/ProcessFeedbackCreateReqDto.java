package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessFeedbackCreateReqDto(
        @JsonProperty("content")
        String content
) {}
