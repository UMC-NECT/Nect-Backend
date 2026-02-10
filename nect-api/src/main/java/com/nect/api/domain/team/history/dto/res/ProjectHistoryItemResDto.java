package com.nect.api.domain.team.history.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.user.enums.RoleField;

import java.time.LocalDateTime;

public record ProjectHistoryItemResDto(
        @JsonProperty("history_id")
        Long historyId,

        @JsonProperty("action")
        HistoryAction action,

        @JsonProperty("target_type")
        HistoryTargetType targetType,

        @JsonProperty("target_id")
        Long targetId,

        @JsonProperty("actor")
        ActorDto actor,

        @JsonProperty("main_message")
        String mainMessage,

        @JsonProperty("content_message")
        String contentMessage,

        @JsonProperty("created_at")
        LocalDateTime createdAt

) {
    public record ActorDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("name")
            String name,

            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("role_field")
            RoleField roleField,

            @JsonProperty("custom_field_name")
            String customFieldName
    ) {}
}
