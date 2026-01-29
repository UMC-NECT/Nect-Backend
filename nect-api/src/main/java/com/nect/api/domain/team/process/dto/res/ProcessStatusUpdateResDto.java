package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.time.LocalDateTime;

public record ProcessStatusUpdateResDto(
        @JsonProperty("process_id")
        Long processId,

        @JsonProperty("status")
        ProcessStatus status,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {}
