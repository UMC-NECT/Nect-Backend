package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.time.LocalDate;
import java.util.List;

public record ProcessOrderUpdateReqDto(
        @JsonProperty("status")
        ProcessStatus status,

        @JsonProperty("ordered_process_ids")
        List<Long> orderedProcessIds,

        @JsonProperty("lane_key")
        String laneKey,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("dead_line")
        LocalDate deadLine
) {}
