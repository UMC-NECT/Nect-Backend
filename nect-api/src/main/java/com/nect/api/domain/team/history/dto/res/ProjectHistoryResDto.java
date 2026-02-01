package com.nect.api.domain.team.history.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;

import java.time.LocalDateTime;

public record ProjectHistoryResDto(
        @JsonProperty("history_id")
        Long historyId,

        @JsonProperty("actor_user_id")
        Long actorUserId,

        @JsonProperty("action")
        HistoryAction action,

        @JsonProperty("target_type")
        HistoryTargetType targetType,

        @JsonProperty("target_id")
        Long targetId,

        @JsonProperty("meta_json")
        String metaJson,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}
