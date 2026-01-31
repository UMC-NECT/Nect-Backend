package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.enums.FileExt;

import java.time.LocalDateTime;
import java.util.List;

public record SharedDocumentsPreviewResDto(
        @JsonProperty("documents")
        List<DocumentDto> documents
) {
    public record DocumentDto(
            @JsonProperty("document_id")
            Long documentId,

            @JsonProperty("is_pinned")
            boolean isPinned,

            @JsonProperty("title")
            String title,

            @JsonProperty("file_name")
            String fileName,

            @JsonProperty("file_ext")
            FileExt fileExt,

            @JsonProperty("file_url")
            String fileUrl,

            @JsonProperty("file_size")
            Long fileSize,

            @JsonProperty("created_at")
            LocalDateTime createdAt,

            @JsonProperty("uploader")
            UploaderDto uploader
    ) {}

    public record UploaderDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("name")
            String name,

            @JsonProperty("nickname")
            String nickname,

            // TODO: UserProfile 엔티티 생기면 연결
            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {}
}