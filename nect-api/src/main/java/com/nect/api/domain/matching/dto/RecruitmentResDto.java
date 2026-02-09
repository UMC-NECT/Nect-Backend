package com.nect.api.domain.matching.dto;

import com.nect.core.entity.user.enums.RoleField;
import lombok.Builder;

import java.util.List;

public class RecruitmentResDto {

    @Builder
    public record RecruitingFieldDto(
            RoleField field,
            String customField
    ){}

    @Builder
    public record EnrollRecruitmentResDto(
            Long recruitmentId,
            RoleField roleField,
            String customField,
            Integer capacity,
            List<String> requirements
    ){}
}
