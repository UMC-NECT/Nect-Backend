package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessFileAttachResDto(
        @JsonProperty("file_id")
        Long fileId
) {}