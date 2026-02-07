package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.api.domain.team.process.dto.res.AttachmentDto;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.workspace.enums.PostType;

import java.time.LocalDateTime;
import java.util.List;

public record PostGetResDto(
        @JsonProperty("post_id")
        Long postId,

        @JsonProperty("post_type")
        PostType postType,

        @JsonProperty("title")
        String title,

        @JsonProperty("content")
        String content,

        @JsonProperty("like_count")
        Long likeCount,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("author")
        AuthorDto author,

        @JsonProperty("attachments")
        List<PostAttachmentResDto> attachments
) {
    public record AuthorDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("name")
            String name,

            @JsonProperty("nickname")
            String nickname
    ) {}
}
