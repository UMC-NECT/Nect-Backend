package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.api.domain.team.process.enums.LaneType;

public record LaneProgressResDto(
        @JsonProperty("lane_key")  // "ROLE:BACKEND" / "CUSTOM:영상편집"
        String laneKey,
        @JsonProperty("lane_type") // ROLE / CUSTOM
        LaneType laneType,
        @JsonProperty("lane_name")  // "BACKEND" / "영상편집"
        String laneName,

        @JsonProperty("planning")
        long planning,

        @JsonProperty("in_progress")
        long inProgress,

        @JsonProperty("done")
        long done,

        @JsonProperty("total")
        long total,

        @JsonProperty("planning_rate")
        Integer planningRate,

        @JsonProperty("in_progress_rate")
        Integer inProgressRate,

        @JsonProperty("done_rate")
        Integer doneRate
) {}

