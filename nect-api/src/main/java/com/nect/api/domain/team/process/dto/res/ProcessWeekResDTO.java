package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProcessWeekResDTO(
        @JsonProperty("start_date")
        String startDate,

        @JsonProperty("project_purpose")
        List<ProcessCardResDTO> projectPurpose,

        @JsonProperty("by_field")
        List<FieldGroupResDTO> byField
) {}
