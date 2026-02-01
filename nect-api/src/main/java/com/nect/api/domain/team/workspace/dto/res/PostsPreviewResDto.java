package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.workspace.enums.PostType;

import java.time.LocalDateTime;
import java.util.List;

public record PostsPreviewResDto(
        @JsonProperty("posts")
        List<Item> posts
) {
    public record Item(
            @JsonProperty("post_id")
            Long postId,

            @JsonProperty("post_type")
            PostType postType,

            @JsonProperty("title")
            String title,

            @JsonProperty("is_pinned")
            Boolean isPinned,

            @JsonProperty("created_at")
            LocalDateTime createdAt
    ) {}
}