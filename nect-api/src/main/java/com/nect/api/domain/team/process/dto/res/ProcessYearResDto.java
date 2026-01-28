package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public record ProcessYearResDto(
        @JsonProperty("year")
        Integer year,

        @JsonProperty("months")
        List<ProcessYearMonthSummaryResDto> months
) {
    public record ProcessYearMonthSummaryResDto(
            @JsonProperty("year_month")
            String yearMonth, // "2026-01"

            @JsonProperty("total_count")
            Integer totalCount,

            @JsonProperty("status_counts")
            Map<ProcessStatus, Integer> statusCounts
    ) {}
}