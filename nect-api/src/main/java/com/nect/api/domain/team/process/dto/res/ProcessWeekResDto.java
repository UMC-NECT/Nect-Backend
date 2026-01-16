package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProcessWeekResDto(
        @JsonProperty("start_date")
        String startDate,

        @JsonProperty("project_purpose")
        List<ProcessCardResDto> projectPurpose,

        @JsonProperty("by_field")
        List<FieldGroupResDto> byField
) {}
