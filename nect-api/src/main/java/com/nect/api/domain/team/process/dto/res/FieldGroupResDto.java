package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FieldGroupResDto(
        @JsonProperty("field_id")
        String fieldId,          // "ROLE:BACKEND" / "CUSTOM:영상편집" 같은 laneKey

        @JsonProperty("field_name")
        String fieldName,        // "BACKEND" / "영상편집"

        @JsonProperty("field_order")
        Integer fieldOrder,      // ROLE=0, CUSTOM=1 같은 타입 order(정해진 우선순위 없음)

        List<ProcessCardResDto> processes
) {}