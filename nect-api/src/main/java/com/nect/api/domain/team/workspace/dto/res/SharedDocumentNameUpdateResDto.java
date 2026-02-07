package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SharedDocumentNameUpdateResDto(
        @JsonProperty("document_id")
        Long documentId,

        @JsonProperty("title")
        String title
) {}
