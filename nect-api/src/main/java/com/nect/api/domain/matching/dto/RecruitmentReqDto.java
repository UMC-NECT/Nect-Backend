package com.nect.api.domain.matching.dto;

import com.nect.core.entity.user.enums.RoleField;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public class RecruitmentReqDto {

    @Builder
    public record EnrollRecruitmentReqDto(
            @NotNull
            RoleField roleField,
            String customField,
            @NotNull
            Integer capacity,
            List<String> requirements
    ){}
}
