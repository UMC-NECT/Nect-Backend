package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.time.LocalDate;

public record ProcessOrderUpdateResDto(
        @JsonProperty("process_id")
        Long processId,

        @JsonProperty("status")
        ProcessStatus status,

        @JsonProperty("status_order")
        Integer statusOrder,

        @JsonProperty("start_at")
        LocalDate startAt,

        @JsonProperty("dead_line")
        LocalDate deadLine
) {}
