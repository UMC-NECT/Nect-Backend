package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessFileAttachReqDto(
        @JsonProperty("file_id")
        Long fileId
){}
