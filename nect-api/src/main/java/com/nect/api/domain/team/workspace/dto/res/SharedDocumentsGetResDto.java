package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.enums.FileExt;

import java.time.LocalDateTime;
import java.util.List;

public record SharedDocumentsGetResDto(
        @JsonProperty("page")
        int page,

        @JsonProperty("size")
        int size,

        @JsonProperty("total_elements")
        long totalElements,

        @JsonProperty("total_pages")
        int totalPages,

        @JsonProperty("documents")
        List<DocumentDto> documents
) {
    public record DocumentDto(
            @JsonProperty("document_id")
            Long documentId,

            @JsonProperty("is_pinned")
            boolean isPinned,

            @JsonProperty("document_type")
            DocumentType documentType,

            @JsonProperty("title")
            String title,


            @JsonProperty("file_name")
            String fileName,

            @JsonProperty("file_ext")
            FileExt fileExt,

            // FILE: fileKey, LINK: null
            @JsonProperty("file_url")
            String fileUrl,

            // LINK: url, FILE: null
            @JsonProperty("link_url")
            String linkUrl,

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

            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {}
}
