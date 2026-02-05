package com.nect.api.domain.matching.service;

import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.api.domain.matching.enums.code.RecruitmentErrorCode;
import com.nect.api.domain.matching.exception.RecruitmentException;
import com.nect.api.domain.team.project.converter.ProjectConverter;
import com.nect.api.domain.team.project.dto.RecruitingProjectResDto;
import com.nect.api.domain.team.project.service.ProjectService;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.matching.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    private final ProjectService projectService;

    public void validateRecruitable(Project project, RoleField field){
        Recruitment recruitment = recruitmentRepository
                .findRecruitmentByProjectAndField(project, field)
                .orElseThrow(
                        () -> new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN)
                );

        if (recruitment.getCapacity() < 1){
            throw new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN);
        }
    }

    public void consumeIfAcceptable(Matching matching, Project project){
        RoleField field = matching.getField();

        Recruitment recruitment = recruitmentRepository
                .findRecruitmentByProjectAndField(project, field)
                .orElseThrow(
                        () -> new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN)
                );

        if (recruitment.getCapacity() < 1){
            throw new RecruitmentException(RecruitmentErrorCode.RECRUITMENT_NOT_OPEN);
        }

        recruitment.decreaseCapacity();
    }

    public List<RecruitmentResDto.RecruitingFieldDto> findRecruitingFields(Long projectId) {
        Project project = projectService.getProject(projectId);
        List<Recruitment> recruitments = recruitmentRepository.findOpenFieldsByProject(project);

        return recruitments.stream()
                .map(recruitment -> RecruitmentResDto.RecruitingFieldDto.builder()
                        .field(recruitment.getField())
                        .customField(recruitment.getCustomField())
                        .build()
                )
                .toList();
    }

    public List<RecruitingProjectResDto> getMyRecruitingProjectAsLeader(Long userId) {
        List<Project> projects= projectService.getProjectsAsLeader(userId);

        return projects.stream().map(ProjectConverter::toRecruitingProjectResDto).toList();
    }
}
