package com.nect.api.domain.matching.dto;

import com.nect.core.entity.user.enums.RoleField;
import lombok.Builder;

public class RecruitmentResDto {

    @Builder
    public record RecruitingFieldDto(
            RoleField field,
            String customField
    ){}
}
