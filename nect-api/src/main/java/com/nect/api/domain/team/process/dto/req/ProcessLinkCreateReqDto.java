package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessLinkCreateReqDto(
        @JsonProperty("title")
        String title,

        @JsonProperty("link_url")
        String linkUrl
) {}
