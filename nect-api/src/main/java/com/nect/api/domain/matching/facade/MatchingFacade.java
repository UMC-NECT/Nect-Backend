package com.nect.api.domain.matching.facade;

import com.nect.api.domain.matching.converter.MatchingConverter;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.service.MatchingService;
import com.nect.api.domain.matching.service.RecruitmentService;
import com.nect.api.domain.team.project.converter.ProjectUserConverter;
import com.nect.api.domain.team.project.service.ProjectService;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingFacade {

    private final MatchingService matchingService;
    private final RecruitmentService recruitmentService;
    private final ProjectService projectService;
    private final ProjectUserService projectUserService;

    @Transactional
    public MatchingResDto.MatchingRes createUserToProjectMatching(
            Long requestUserId,
            Long projectId,
            Long fieldId
    ){
        Project project = projectService.getProject(projectId);

        recruitmentService.validateRecruitable(project, fieldId);
        Matching matching = matchingService.createUserToProjectMatching(requestUserId, projectId, fieldId);
        return MatchingConverter.toMatchingResDto(matching);
    }

    @Transactional
    public MatchingResDto.MatchingRes createProjectToUserMatching(
            Long requestUserId, Long targetUserId, Long projectId, Long fieldId
    ){
        Project project = projectService.getProject(projectId);

        recruitmentService.validateRecruitable(project, fieldId);
        Matching matching = matchingService.createProjectToUserMatching(requestUserId, targetUserId, projectId, fieldId);
        return MatchingConverter.toMatchingResDto(matching);
    }

    @Transactional
    public MatchingResDto.MatchingAcceptResDto acceptMatchingRequest(
            Long matchingId, Long userId
    ) {
        Matching matching = matchingService.getMatching(matchingId);
        Project project = projectService.getProject(matching.getProjectId());
        Long joinUserId = matching.getTargetUserId();

        if (matching.getRequestType() == MatchingRequestType.USER_TO_PROJECT){
            projectUserService.validateLeader(project, userId);
            joinUserId = matching.getRequestUserId();
        }

        recruitmentService.consumeIfAcceptable(matching, project);
        Matching acceptedMatching = matchingService.acceptMatching(matchingId, userId);
        ProjectUser projectUser = projectUserService.addProjectUser(joinUserId, project, matching.getFieldId());

        return MatchingResDto.MatchingAcceptResDto.builder()
                .matching(MatchingConverter.toMatchingResDto(acceptedMatching))
                .projectUser(ProjectUserConverter.toProjectUserResDto(projectUser))
                .build();
    }

    @Transactional
    public MatchingResDto.MatchingRes rejectMatching(
            Long matchingId, Long userId
    ){
        Matching matching = matchingService.getMatching(matchingId);
        Project project = projectService.getProject(matching.getProjectId());

        if (matching.getRequestType() == MatchingRequestType.USER_TO_PROJECT){
            projectUserService.validateLeader(project, userId);
        }

        Matching rejectedMatching = matchingService.rejectMatchingRequest(matchingId, userId);
        return MatchingConverter.toMatchingResDto(rejectedMatching);
    }
}
