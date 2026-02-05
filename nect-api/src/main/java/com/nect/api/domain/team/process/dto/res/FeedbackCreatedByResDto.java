package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FeedbackCreatedByResDto(
        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("user_name")
        String userName,

        @JsonProperty("nickname")
        String nickname,

        @JsonProperty("role_fields")
        List<String> roleFields
){}

