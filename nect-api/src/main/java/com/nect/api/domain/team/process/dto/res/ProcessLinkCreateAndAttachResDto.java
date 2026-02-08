package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessLinkCreateAndAttachResDto(
        @JsonProperty("document_id")
        Long documentId,

        @JsonProperty("title")
        String title,

        @JsonProperty("url")
        String url
) {}
