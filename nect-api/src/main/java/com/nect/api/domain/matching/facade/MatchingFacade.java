package com.nect.api.domain.matching.facade;

import com.nect.api.domain.matching.converter.MatchingConverter;
import com.nect.api.domain.matching.dto.MatchingReqDto;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.service.MatchingService;
import com.nect.api.domain.matching.service.RecruitmentService;
import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.api.domain.team.project.converter.ProjectUserConverter;
import com.nect.api.domain.team.project.service.ProjectService;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.api.domain.user.service.UserService;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRejectReason;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.user.User;
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
    private final UserService userService;
    private final NotificationFacade notificationFacade;

    @Transactional
    public MatchingResDto.MatchingRes createUserToProjectMatching(
            Long requestUserId,
            Long projectId,
            MatchingReqDto.matchingReqDto reqDto
    ){
        User requestUser = userService.getUser(requestUserId);
        Project project = projectService.getProject(projectId);

        recruitmentService.validateRecruitable(project, reqDto.field());
        Matching matching = matchingService.createUserToProjectMatching(
                requestUser,
                project,
                reqDto.field(),
                reqDto.customField());

        notificationFacade.notify(
                matching.getTargetUser(), NotificationCommand.builder()
                        .type(NotificationType.MATCHING_REQUEST_RECEIVED)
                        .classification(NotificationClassification.MY_PAGE)
                        .scope(NotificationScope.MAIN_HOME)
                        .targetId(matching.getTargetUser().getUserId())
                        .mainArgs(new Object[]{})
                        .project(matching.getProject())
                        .build()
        );

        return MatchingConverter.toMatchingResDto(matching);
    }

    @Transactional
    public MatchingResDto.MatchingRes createProjectToUserMatching(
            Long requestUserId,
            Long targetUserId,
            Long projectId,
            MatchingReqDto.matchingReqDto reqDto
    ){
        User requestUser = userService.getUser(requestUserId);
        User targetUser = userService.getUser(targetUserId);
        Project project = projectService.getProject(projectId);

        recruitmentService.validateRecruitable(project, reqDto.field());
        Matching matching = matchingService.createProjectToUserMatching(
                requestUser,
                targetUser,
                project,
                reqDto.field(),
                reqDto.customField()
        );

        notificationFacade.notify(
                matching.getTargetUser(), NotificationCommand.builder()
                        .type(NotificationType.MATCHING_REQUEST_RECEIVED)
                        .classification(NotificationClassification.MY_PAGE)
                        .scope(NotificationScope.MAIN_HOME)
                        .targetId(matching.getTargetUser().getUserId())
                        .mainArgs(new Object[]{})
                        .project(matching.getProject())
                        .build()
        );

        return MatchingConverter.toMatchingResDto(matching);
    }

    @Transactional
    public MatchingResDto.MatchingAcceptResDto acceptMatchingRequest(
            Long matchingId, Long userId
    ) {
        Matching matching = matchingService.getMatching(matchingId);
        User user = userService.getUser(userId);
        Project project = matching.getProject();
        User joinUser = matching.getTargetUser();

        if (matching.getRequestType() == MatchingRequestType.USER_TO_PROJECT){
            projectUserService.validateLeader(project, user);
            joinUser = matching.getRequestUser();
        }

        recruitmentService.consumeIfAcceptable(matching, project);
        Matching acceptedMatching = matchingService.acceptMatching(matchingId, user);
        ProjectUser projectUser = projectUserService.addProjectUser(joinUser.getUserId(), project, matching.getField());

        notificationFacade.notify(
                matching.getRequestUser(), NotificationCommand.builder()
                        .type(NotificationType.MATCHING_ACCEPTED)
                        .classification(NotificationClassification.MY_PAGE)
                        .scope(NotificationScope.MAIN_HOME)
                        .targetId(matching.getRequestUser().getUserId())
                        .mainArgs(new Object[]{})
                        .project(matching.getProject())
                        .build()
        );

        return MatchingResDto.MatchingAcceptResDto.builder()
                .matching(MatchingConverter.toMatchingResDto(acceptedMatching))
                .projectUser(ProjectUserConverter.toProjectUserResDto(projectUser))
                .build();
    }

    @Transactional
    public MatchingResDto.MatchingRes rejectMatching(
            Long matchingId, Long userId, MatchingRejectReason rejectReason
    ){
        Matching matching = matchingService.getMatching(matchingId);
        User user = userService.getUser(userId);
        Project project = matching.getProject();

        if (matching.getRequestType() == MatchingRequestType.USER_TO_PROJECT){
            projectUserService.validateLeader(project, user);
        }

        Matching rejectedMatching = matchingService.rejectMatchingRequest(matchingId, user, rejectReason);

        String targetName = (rejectedMatching.getRequestType() == MatchingRequestType.USER_TO_PROJECT)
                ? rejectedMatching.getProject().getTitle()
                : rejectedMatching.getTargetUser().getName();

        notificationFacade.notify(
                rejectedMatching.getRequestUser(), NotificationCommand.builder()
                        .type(NotificationType.MATCHING_REJECTED)
                        .classification(NotificationClassification.MY_PAGE)
                        .scope(NotificationScope.MAIN_HOME)
                        .targetId(rejectedMatching.getRequestUser().getUserId())
                        .mainArgs(new Object[]{targetName})
                        .project(rejectedMatching.getProject())
                        .build()
        );

        return MatchingConverter.toMatchingResDto(rejectedMatching);
    }

    @Transactional
    public MatchingResDto.MatchingRes cancelMatching(Long matchingId, Long userId){
        Matching matching = matchingService.getMatching(matchingId);
        User user = userService.getUser(userId);

        Matching canceledMatching = matchingService.cancelMatching(matching, user);

        return MatchingConverter.toMatchingResDto(canceledMatching);
    }
}
