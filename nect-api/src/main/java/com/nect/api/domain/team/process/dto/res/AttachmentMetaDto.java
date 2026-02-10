package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.api.domain.team.process.enums.AttachmentType;
import com.nect.core.entity.team.enums.FileExt;

import java.time.LocalDateTime;

public record AttachmentMetaDto(
        @JsonProperty("type")
        AttachmentType type,

        @JsonProperty("document_id")
        Long documentId,

        @JsonProperty("attached_at")
        LocalDateTime attachedAt,

        @JsonProperty("file_ext")
        FileExt fileExt
) {}