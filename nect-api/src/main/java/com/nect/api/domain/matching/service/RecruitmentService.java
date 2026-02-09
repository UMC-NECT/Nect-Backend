package com.nect.api.domain.matching.service;

import com.nect.api.domain.matching.converter.RecruitmentConverter;
import com.nect.api.domain.matching.dto.RecruitmentReqDto;
import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.api.domain.matching.enums.code.RecruitmentErrorCode;
import com.nect.api.domain.matching.exception.RecruitmentException;
import com.nect.api.domain.team.project.converter.ProjectConverter;
import com.nect.api.domain.team.project.dto.RecruitingProjectResDto;
import com.nect.api.domain.team.project.service.ProjectService;
import com.nect.api.domain.user.service.UserService;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.matching.RecruitmentRequirement;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.matching.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    private final ProjectService projectService;
    private final UserService userService;

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

    @Transactional
    public RecruitmentResDto.EnrollRecruitmentResDto enrollRecruitment(
            Long userId,
            Long projectId,
            RecruitmentReqDto.EnrollRecruitmentReqDto reqDto
    ) {
        User user = userService.getUser(userId);
        Project project = projectService.getProject(projectId);

        if (!(user.getUserId().equals(projectService.getLeader(project).getUserId()))){
            throw new RecruitmentException(RecruitmentErrorCode.ONLY_LEADER_ACCESS);
        }

        Recruitment recruitment = Recruitment.builder()
                .project(project)
                .field(reqDto.roleField())
                .capacity(reqDto.capacity())
                .customField(reqDto.customField())
                .build();

        for (int i = 0; i < reqDto.requirements().size(); i++) {
            recruitment.addRequirement(RecruitmentRequirement.builder()
                    .content(reqDto.requirements().get(i))
                    .sortOrder(i)
                    .build()
            );
        }

        Recruitment saved = recruitmentRepository.save(recruitment);
        return RecruitmentConverter.toEnrollResDto(saved);
    }

    @Transactional
    public RecruitmentResDto.EnrollRecruitmentResDto updateRecruitment(
            Long userId,
            Long projectId,
            Long recruitmentId,
            RecruitmentReqDto.EnrollRecruitmentReqDto reqDto
    ) {
        User user = userService.getUser(userId);
        Project project = projectService.getProject(projectId);

        if (!(user.getUserId().equals(projectService.getLeader(project).getUserId()))){
            throw new RecruitmentException(RecruitmentErrorCode.ONLY_LEADER_ACCESS);
        }

        Recruitment recruitment = recruitmentRepository.findByIdAndProject(recruitmentId, project)
                .orElseThrow(() -> new RecruitmentException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));

        recruitment.updateField(reqDto.roleField());
        recruitment.updateCustomField(reqDto.customField());
        recruitment.updateCapacity(reqDto.capacity());

        recruitment.getRequirements().clear();

        for (int i = 0; i < reqDto.requirements().size(); i++) {
            recruitment.addRequirement(
                    RecruitmentRequirement.builder()
                            .content(reqDto.requirements().get(i))
                            .sortOrder(i)
                            .build()
            );
        }

        return RecruitmentConverter.toEnrollResDto(recruitment);
    }

    public List<RecruitmentResDto.EnrollRecruitmentResDto> getRecruitmentsByProject(Long projectId) {
        Project project = projectService.getProject(projectId);

        List<Recruitment> recruitments = recruitmentRepository.findByProject(project);

        return recruitments.stream()
                .map(RecruitmentConverter::toEnrollResDto)
                .toList();
    }
}
