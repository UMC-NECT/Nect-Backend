package com.nect.api.domain.team.workspace.dto.res;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record CalendarMonthIndicatorsResDto(

        @JsonProperty("year")
        int year,

        @JsonProperty("month")
        int month,

        @JsonProperty("days")
        List<DayIndicator> days

) {
    public record DayIndicator(
            @JsonProperty("date")
            LocalDate date,

            @JsonProperty("event_count")
            int eventCount
    ) {}
}
