package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record PostUpdateResDto(
        @JsonProperty("post_id")
        Long postId,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {}
