package com.nect.api.domain.matching.service;

import com.nect.api.domain.matching.enums.code.RecruitmentErrorCode;
import com.nect.api.domain.matching.exception.RecruitmentException;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import com.nect.core.repository.matching.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    public void validateRecruitable(Project project, Long fieldId){
        Recruitment recruitment = recruitmentRepository
                .findRecruitmentByProjectAndFieldId(project, fieldId)
                .orElseThrow(
                        () -> new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN)
                );

        if (recruitment.getCapacity() < 1){
            throw new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN);
        }
    }

    public void consumeIfAcceptable(Matching matching, Project project){
        Long fieldId = matching.getFieldId();

        Recruitment recruitment = recruitmentRepository
                .findRecruitmentByProjectAndFieldId(project, fieldId)
                .orElseThrow(
                        () -> new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN)
                );

        if (recruitment.getCapacity() < 1){
            throw new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN);
        }

        recruitment.decreaseCapacity();
    }
}
