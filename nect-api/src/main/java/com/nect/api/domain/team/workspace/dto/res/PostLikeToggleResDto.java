package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostLikeToggleResDto(
        @JsonProperty("post_id")
        Long postId,

        @JsonProperty("liked")
        boolean liked, // true : 좋아요 등록, false : 좋아요 취소

        @JsonProperty("like_count")
        Long likeCount
) {}
