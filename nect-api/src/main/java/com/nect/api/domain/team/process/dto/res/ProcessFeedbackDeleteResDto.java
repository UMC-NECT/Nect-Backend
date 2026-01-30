package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessFeedbackDeleteResDto(
        @JsonProperty("deleted_feedback_id")
        Long deletedFeedbackId
) {
}
