package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

import java.time.LocalDateTime;

public record ProcessCreateResDto(
        @JsonProperty("process_id")
        Long processId,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("writer")
        WriterDto writer
) {
    public record WriterDto(
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