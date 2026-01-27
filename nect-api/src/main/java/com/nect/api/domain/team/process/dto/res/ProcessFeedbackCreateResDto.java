package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessFeedbackStatus;

import java.time.LocalDateTime;

public record ProcessFeedbackCreateResDto(
        @JsonProperty("feedback_id")
        Long feedbackId,

        String content,

        ProcessFeedbackStatus status,

        // TODO : 피드백 생성 관련 dto 생성할 때 만들 예정
        @JsonProperty("created_by")
        FeedbackCreatedByResDto createdBy,

        @JsonProperty("created_at")
        LocalDateTime createdAt

) {}
