package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProcessPartResDto(
        @JsonProperty("field_id")
        Long fieldId,

        @JsonProperty("status_groups")
        List<ProcessStatusGroupResDto> statusGroups
) {}

