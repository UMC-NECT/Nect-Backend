package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkResDto(
        @JsonProperty("link_id")
        Long linkId,

        String url
) {
}
