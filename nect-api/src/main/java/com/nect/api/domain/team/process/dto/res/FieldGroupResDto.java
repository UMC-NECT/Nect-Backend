package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FieldGroupResDto(
        @JsonProperty("field_id")
        Long fieldId,

        @JsonProperty("field_name")
        String fieldName,

        @JsonProperty("field_order")
        Integer fieldOrder,

        List<ProcessCardResDto> processes
) {}
