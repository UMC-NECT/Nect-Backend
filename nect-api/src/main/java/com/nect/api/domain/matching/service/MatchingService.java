package com.nect.api.domain.matching.service;

import com.nect.api.domain.matching.converter.MatchingConverter;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.enums.CounterParty;
import com.nect.api.domain.matching.enums.code.MatchingErrorCode;
import com.nect.api.domain.matching.exception.MatchingException;
import com.nect.api.domain.team.project.service.ProjectService;
import com.nect.api.domain.user.service.UserService;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRejectReason;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.matching.MatchingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingRepository matchingRepository;
    private final UserService userService;
    private final ProjectService projectService;

    public Matching createUserToProjectMatching(
            User requestUser,
            Project project,
            RoleField field,
            String customField
    ) {
        User targetUser = projectService.getLeader(project);

        if (matchingRepository.countByRequestTypeAndRequestUserAndMatchingStatus(
                MatchingRequestType.USER_TO_PROJECT,
                requestUser,
                MatchingStatus.PENDING
        ) >= 1){
            throw new MatchingException(MatchingErrorCode.MATCHING_APPLY_COUNT_EXCEEDED);
        }

        if (field == RoleField.CUSTOM) {
            if (customField == null || customField.isBlank()){
                throw new MatchingException(MatchingErrorCode.MATCHING_CUSTOM_FIELD_REQUIRED);
            }
        }

        Matching matching = MatchingConverter
                .toMatching(requestUser, targetUser, project, field, MatchingRequestType.USER_TO_PROJECT, customField);
        matchingRepository.save(matching);
        return matching;
    }

    public Matching createProjectToUserMatching(
            User requestUser, User targetUser, Project project, RoleField field, String customField
    ) {
        if (matchingRepository.countByRequestTypeAndProjectAndFieldAndMatchingStatus(
                MatchingRequestType.PROJECT_TO_USER,
                project,
                field,
                MatchingStatus.PENDING
        ) > 3) {
            throw new MatchingException(MatchingErrorCode.MATCHING_INVITE_COUNT_EXCEEDED);
        }

        if (field == RoleField.CUSTOM) {
            if (customField == null || customField.isBlank()){
                throw new MatchingException(MatchingErrorCode.MATCHING_CUSTOM_FIELD_REQUIRED);
            }
        }

        Matching matching = MatchingConverter.toMatching(
                requestUser, targetUser, project, field, MatchingRequestType.PROJECT_TO_USER, customField
        );
        matchingRepository.save(matching);
        return matching;
    }

    public Matching cancelMatching(Matching matching, User user) {
        if (!(matching.getRequestUser().equals(user))) {
            throw new MatchingException(MatchingErrorCode.MATCHING_ACCESS_DENIED);
        }

        if (matching.getMatchingStatus() != MatchingStatus.PENDING){
            throw new MatchingException(MatchingErrorCode.MATCHING_STATUS_NOT_CANCELABLE);
        }

        matching.changeStatus(MatchingStatus.CANCELED);
        return matching;
    }

    public Matching acceptMatching(Long matchingId, User user) {
        Matching matching = getMatching(matchingId);

        if (!(matching.getTargetUser().equals(user))){
            throw new MatchingException(MatchingErrorCode.MATCHING_ACCESS_DENIED);
        }

        if (matching.getMatchingStatus() != MatchingStatus.PENDING){
            throw new MatchingException(MatchingErrorCode.MATCHING_STATUS_NOT_ACCEPTABLE);
        }

        matching.changeStatus(MatchingStatus.ACCEPTED);
        return matching;
    }

    @Transactional(readOnly = true)
    public MatchingResDto.MatchingListRes getReceivedMatchingsByTarget(
            Long userId,
            CounterParty counterParty,
            MatchingStatus matchingStatus
    ) {
        User user = userService.getUser(userId);
        List<Matching> pendingMatchings;

        if (counterParty == CounterParty.USER){
            pendingMatchings = matchingRepository.findReceivedMatchingsOrderByExpiresAt(
                    MatchingRequestType.USER_TO_PROJECT,
                    user,
                    matchingStatus
            );

            List<MatchingResDto.UserSummary> userSummaries = pendingMatchings.stream()
                    .map(Matching::getRequestUser)
                    .map(MatchingConverter::toUserSummary)
                    .toList();

            return MatchingResDto.MatchingListRes.builder()
                    .counterParty(counterParty)
                    .userMatchings(userSummaries)
                    .projectMatchings(List.of()) // 빈 리스트로 반환
                    .build();
        }else if (counterParty == CounterParty.PROJECT){
            pendingMatchings = matchingRepository.findReceivedMatchingsOrderByExpiresAt(
                    MatchingRequestType.PROJECT_TO_USER,
                    user,
                    matchingStatus
            );

            List<MatchingResDto.ProjectSummary> projectSummaries = pendingMatchings.stream()
                    .map(Matching::getProject)
                    .map(project -> MatchingConverter.toProjectSummary(
                            project,
                            projectService.getUserNumberOfProject(project)
                        )
                    )
                    .toList();

            return MatchingResDto.MatchingListRes.builder()
                    .counterParty(counterParty)
                    .userMatchings(List.of()) //빈 리스트로 반환
                    .projectMatchings(projectSummaries)
                    .build();
        }

        throw new MatchingException(MatchingErrorCode.NOT_INVALID_COUNTERPARTY);
    }

    @Transactional(readOnly = true)
    public MatchingResDto.MatchingListRes getSentMatchingsByTarget(
            Long userId,
            CounterParty counterParty,
            MatchingStatus matchingStatus
    ) {
        User user = userService.getUser(userId);
        List<Matching> pendingMatchings;

        if (counterParty == CounterParty.USER){
            pendingMatchings = matchingRepository.findSentMatchingsOrderByExpiresAt(
                    MatchingRequestType.PROJECT_TO_USER, user, matchingStatus
            );

            List<MatchingResDto.UserSummary> userSummaries = pendingMatchings.stream()
                    .map(Matching::getTargetUser)
                    .map(MatchingConverter::toUserSummary)
                    .toList();

            return MatchingResDto.MatchingListRes.builder()
                    .counterParty(counterParty)
                    .userMatchings(userSummaries)
                    .projectMatchings(List.of()) // 빈 리스트로 반환
                    .build();
        }else if(counterParty == CounterParty.PROJECT){
            pendingMatchings = matchingRepository.findSentMatchingsOrderByExpiresAt(
                    MatchingRequestType.USER_TO_PROJECT, user, matchingStatus
            );

            List<MatchingResDto.ProjectSummary> projectSummaries = pendingMatchings.stream()
                    .map(Matching::getProject)
                    .map(project -> MatchingConverter.toProjectSummary(
                            project,
                            projectService.getUserNumberOfProject(project)
                            )
                    )
                    .toList();

            return MatchingResDto.MatchingListRes.builder()
                    .counterParty(counterParty)
                    .userMatchings(List.of()) //빈 리스트로 반환
                    .projectMatchings(projectSummaries)
                    .build();
        }

        throw new MatchingException(MatchingErrorCode.NOT_INVALID_COUNTERPARTY);
    }

    public Matching rejectMatchingRequest(Long matchingId, User user, MatchingRejectReason rejectReason) {
        Matching matching = getMatching(matchingId);

        if (!(matching.getTargetUser().equals(user))){
            throw new MatchingException(MatchingErrorCode.MATCHING_ACCESS_DENIED);
        }

        if (matching.getMatchingStatus() != MatchingStatus.PENDING){
            throw new MatchingException(MatchingErrorCode.MATCHING_STATUS_NOT_REJECTABLE);
        }

        matching.setRejectReason(rejectReason);
        matching.changeStatus(MatchingStatus.REJECTED);
        return matching;
    }

    public Matching getMatching(Long matchingId) {
        return matchingRepository.findById(matchingId)
                .orElseThrow(
                        () -> new MatchingException(MatchingErrorCode.MATCHING_NOT_FOUND)
                );
    }

    @Transactional
    public int expireDueMatchings(){
        return matchingRepository.bulkExpire(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public MatchingResDto.MatchingCounts getMatchingsCount(Long userId) {
        User user = userService.getUser(userId);

        int received = matchingRepository.countByTargetUserAndMatchingStatus(user, MatchingStatus.PENDING);
        int sent = matchingRepository.countByRequestUserAndMatchingStatus(user, MatchingStatus.PENDING);

        return MatchingResDto.MatchingCounts.builder()
                .receivedCount(received).sentCount(sent).build();
    }
}
