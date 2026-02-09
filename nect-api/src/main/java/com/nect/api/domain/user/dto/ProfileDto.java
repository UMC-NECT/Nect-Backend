package com.nect.api.domain.user.dto;

import com.nect.core.entity.user.enums.SkillCategory;
import java.util.List;

public class ProfileDto {
    public record ProfileSetupRequestDto(
            String nickname,
            String birthDate,
            String job,
            String role,
            List<FieldDto> fields,
            List<SkillDto> skills,
            List<String> interests,
            String firstGoal,
            CollaborationStyleDto collaborationStyle
    ) {}

    public record FieldDto(
            String field,
            String customField
    ) {}

    public record SkillDto(
            SkillCategory skillCategory,
            String skill,
            String customSkillName
    ) {}

    public record CollaborationStyleDto(
            Integer planning,
            Integer logic,
            Integer leadership
    ) {}
}
