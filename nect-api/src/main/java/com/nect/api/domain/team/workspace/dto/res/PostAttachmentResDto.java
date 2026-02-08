package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.enums.FileExt;

public record PostAttachmentResDto(
        @JsonProperty("document_id")
        Long documentId,

        @JsonProperty("document_type")
        DocumentType documentType,

        @JsonProperty("title")
        String title,

        @JsonProperty("link_url")
        String linkUrl,

        @JsonProperty("file_name")
        String fileName,

        @JsonProperty("file_ext")
        FileExt fileExt,

        @JsonProperty("file_size")
        Long fileSize,

        @JsonProperty("download_url")
        String downloadUrl
) {}
