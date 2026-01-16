package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FieldGroupResDTO(
    @JsonProperty("field_id")
    Long fieldId,

    @JsonProperty("field_name")
    String fieldName,

    List<ProcessCardResDTO> processes
) {}
