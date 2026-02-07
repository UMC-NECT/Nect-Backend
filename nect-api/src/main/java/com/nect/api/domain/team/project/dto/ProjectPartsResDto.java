package com.nect.api.domain.team.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

import java.util.List;

public record ProjectPartsResDto(
        @JsonProperty("parts")
        List<PartDto> parts
) {
    public ProjectPartsResDto {
        parts = (parts == null) ? List.of() : parts;
    }

    public record PartDto(
            @JsonProperty("part_id")
            Long partId,

            @JsonProperty("role_field")
            RoleField roleField,

            @JsonProperty("custom_role_field_name")
            String customRoleFieldName,

            @JsonProperty("part_label")
            String partLabel,

            @JsonProperty("required_count")
            Integer requiredCount
    ) {}
}
