package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.workspace.enums.PostType;

import java.time.LocalDateTime;
import java.util.List;

public record PostListResDto(
        @JsonProperty("posts")
        List<PostSummaryDto> posts,

        @JsonProperty("page_info")
        PageInfo pageInfo
) {
    public record PostSummaryDto(
            @JsonProperty("post_id")
            Long postId,

            @JsonProperty("post_type")
            PostType postType,

            @JsonProperty("title")
            String title,

            @JsonProperty("content_preview")
            String contentPreview,

            @JsonProperty("is_pinned")
            Boolean isPinned,

            @JsonProperty("like_count")
            Long likeCount,

            @JsonProperty("created_at")
            LocalDateTime createdAt
    ) {}

    public record PageInfo(
            @JsonProperty("page")
            int page,

            @JsonProperty("size")
            int size,

            @JsonProperty("total_elements")
            long totalElements,

            @JsonProperty("total_pages")
            int totalPages,

            @JsonProperty("has_next")
            boolean hasNext
    ) {}
}
