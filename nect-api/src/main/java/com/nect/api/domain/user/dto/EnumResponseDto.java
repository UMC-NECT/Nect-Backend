package com.nect.api.domain.user.dto;

import java.util.List;

public class EnumResponseDto {

    public record EnumValueDto(
            String value,
            String label,
            String labelEn
    ) {}

    public record RoleFieldsResponseDto(
            String role,
            String roleLabel,
            List<EnumValueDto> fields
    ) {}

    public record CategorySkillsResponseDto(
            String category,
            String categoryLabel,
            List<EnumValueDto> skills
    ) {}
}
