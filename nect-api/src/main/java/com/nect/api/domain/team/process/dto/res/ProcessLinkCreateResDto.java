package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ProcessLinkCreateResDto(
        @JsonProperty("link_id")
        Long linkId,

        @JsonProperty("title")
        String title,

        @JsonProperty("url")
        String url,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {}
