package com.nect.api.domain.matching.converter;

import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.matching.RecruitmentRequirement;

public class RecruitmentConverter {

    public static RecruitmentResDto.EnrollRecruitmentResDto toEnrollResDto(
            Recruitment recruitment
    ) {
        return RecruitmentResDto.EnrollRecruitmentResDto.builder()
                .recruitmentId(recruitment.getId())
                .roleField(recruitment.getField())
                .customField(recruitment.getCustomField())
                .capacity(recruitment.getCapacity())
                .requirements(recruitment.getRequirements().stream()
                        .map(RecruitmentRequirement::getContent)
                        .toList())
                .build();
    }
}
