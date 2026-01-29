package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

public record ProcessStatusUpdateReqDto(
        @JsonProperty("status")
        ProcessStatus status
) {}