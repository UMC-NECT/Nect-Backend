package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessLinkCreateReqDto(
        @JsonProperty("url")
        String url
) {}
