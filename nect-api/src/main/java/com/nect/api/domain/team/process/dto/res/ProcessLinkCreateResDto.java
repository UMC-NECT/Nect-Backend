package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessLinkCreateResDto(
        @JsonProperty("link_id") Long linkId
) {}
