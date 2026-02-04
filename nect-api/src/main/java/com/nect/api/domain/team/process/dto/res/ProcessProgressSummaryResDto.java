package com.nect.api.domain.team.process.dto.res;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProcessProgressSummaryResDto(
        @JsonProperty("lanes")
        List<LaneProgressResDto> lanes
) {}