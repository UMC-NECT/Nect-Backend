package com.nect.api.domain.team.workspace.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.workspace.enums.PostType;

import java.util.List;

public record PostCreateReqDto(
        @JsonProperty("post_type")
        PostType postType,

        @JsonProperty("title")
        String title,

        @JsonProperty("content")
        String content,

        @JsonProperty("is_pinned")
        Boolean isPinned,

        @JsonProperty("mention_user_ids")
        List<Long> mentionUserIds
) {}