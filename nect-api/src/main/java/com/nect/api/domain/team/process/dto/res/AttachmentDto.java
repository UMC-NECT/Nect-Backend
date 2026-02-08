package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.api.domain.team.process.enums.AttachmentType;
import com.nect.core.entity.team.enums.FileExt;

import java.time.LocalDateTime;

public record AttachmentDto(
        @JsonProperty("type")
        AttachmentType type,

        // 공통 식별자(파일이면 file_id, 링크면 link_id)
        @JsonProperty("id")
        Long id,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        // LINK 전용
        @JsonProperty("title")
        String title,

        @JsonProperty("url")
        String url,

        // FILE 전용
        @JsonProperty("file_name")
        String fileName,

        @JsonProperty("file_url")
        String fileUrl,

        @JsonProperty("file_type")
        FileExt fileType,

        @JsonProperty("file_size")
        Long fileSize
) {}
