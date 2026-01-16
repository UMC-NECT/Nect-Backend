package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessCreateResDTO(
        @JsonProperty("process_id")
        Long processId
) {}
