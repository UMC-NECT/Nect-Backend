package com.nect.api.domain.team.workspace.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostLinkCreateReqDto(
        @JsonProperty("title")
        String title,

        @JsonProperty("link_url")
        String linkUrl
) {
}
