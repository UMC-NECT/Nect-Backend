package com.nect.api.domain.team.chat.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.enums.DocumentType;

public record SharedDocumentCreateResDto(
        @JsonProperty("document_id")
        Long documentId,
        @JsonProperty("title")
        String title,
        @JsonProperty("document_type")
        DocumentType documentType
) {}