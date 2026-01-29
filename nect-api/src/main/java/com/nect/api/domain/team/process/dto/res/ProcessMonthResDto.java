package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record ProcessMonthResDto(
        @JsonProperty("month_start")
        LocalDate monthStart,

        @JsonProperty("month_end")
        LocalDate monthEnd,

        @JsonProperty("weeks")
        List<ProcessWeekResDto> weeks
) {
}
