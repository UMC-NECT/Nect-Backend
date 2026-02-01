package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProcessPartResDto(
        @JsonProperty("lane_key")
        String laneKey,

        @JsonProperty("groups")
        List<ProcessStatusGroupResDto> groups
) {}

