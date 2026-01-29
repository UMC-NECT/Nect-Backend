package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.util.List;

public record ProcessStatusGroupResDto(
        @JsonProperty("status")
        ProcessStatus status,

        @JsonProperty("count")
        int count,

        @JsonProperty("processes")
        List<ProcessCardResDto> processes
) {

}
