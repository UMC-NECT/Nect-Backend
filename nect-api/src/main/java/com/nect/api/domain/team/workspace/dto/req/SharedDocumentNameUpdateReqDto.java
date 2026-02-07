package com.nect.api.domain.team.workspace.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SharedDocumentNameUpdateReqDto(
        @JsonProperty("title")
        String title,

        @JsonProperty("name")
        String name
) {
    public String resolvedTitle() {
        String v = (title != null) ? title : name;
        return (v == null) ? null : v.trim();
    }
}
