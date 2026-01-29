package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record ProcessWeekResDto(
        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("common_lane")
        List<ProcessCardResDto> commonLane,

        @JsonProperty("by_field")
        List<FieldGroupResDto> byField
) {}
