package com.nect.api.domain.matching.service;

import com.nect.api.domain.matching.converter.MatchingConverter;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.enums.MatchingBox;
import com.nect.api.domain.matching.enums.code.MatchingErrorCode;
import com.nect.api.domain.matching.exception.MatchingException;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.repository.matching.MatchingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingRepository matchingRepository;

    public Matching createUserToProjectMatching(
            Long requestUserId,
            Long projectId,
            Long fieldId
    ) {
        // TODO: Long targetUserId = project.getLeader();

        if (matchingRepository.countByRequestTypeAndRequestUserIdAndMatchingStatus(
                MatchingRequestType.USER_TO_PROJECT,
                requestUserId,
                MatchingStatus.PENDING
        ) >= 1){
            throw new MatchingException(MatchingErrorCode.MATCHING_APPLY_COUNT_EXCEEDED);
        }

        Matching matching = MatchingConverter
                .toMatching(requestUserId, 1L, projectId, fieldId, MatchingRequestType.USER_TO_PROJECT);
        matchingRepository.save(matching);
        return matching;
    }

    public Matching createProjectToUserMatching(
            Long requestUserId, Long targetUserId, Long projectId, Long fieldId
    ) {
        // TODO: Project project = projectRepository.findById(projectId).orElseThrow();

        if (matchingRepository.countByRequestTypeAndProjectIdAndFieldIdAndMatchingStatus(
                MatchingRequestType.PROJECT_TO_USER,
                projectId,
                fieldId,
                MatchingStatus.PENDING
        ) > 3) {
            throw new MatchingException(MatchingErrorCode.MATCHING_INVITE_COUNT_EXCEEDED);
        }

        Matching matching = MatchingConverter.toMatching(
                requestUserId, targetUserId, projectId, fieldId, MatchingRequestType.PROJECT_TO_USER
        );
        matchingRepository.save(matching);
        return matching;
    }

    @Transactional
    public MatchingResDto.MatchingRes cancelMatching(Long matchingId, Long userId) {
        Matching matching = getMatching(matchingId);

        if (!(matching.getRequestUserId().equals(userId))) {
            throw new MatchingException(MatchingErrorCode.MATCHING_ACCESS_DENIED);
        }

        if (matching.getMatchingStatus() != MatchingStatus.PENDING){
            throw new MatchingException(MatchingErrorCode.MATCHING_STATUS_NOT_CANCELABLE);
        }

        matching.changeStatus(MatchingStatus.CANCELED);
        return MatchingConverter.toMatchingResDto(matching);
    }

    public Matching acceptMatching(Long matchingId, Long userId) {
        Matching matching = getMatching(matchingId);

        if (!(matching.getTargetUserId().equals(userId))){
            throw new MatchingException(MatchingErrorCode.MATCHING_ACCESS_DENIED);
        }

        if (matching.getMatchingStatus() != MatchingStatus.PENDING){
            throw new MatchingException(MatchingErrorCode.MATCHING_STATUS_NOT_ACCEPTABLE);
        }

        matching.changeStatus(MatchingStatus.ACCEPTED);
        return matching;
    }

    @Transactional(readOnly = true)
    public MatchingResDto.MatchingListRes getMatchingsByBox(Long userId, MatchingBox matchingBox) {
        List<Matching> pendingMatchings = new ArrayList<>();
        List<Matching> otherMatchings = new ArrayList<>();

        if (matchingBox == MatchingBox.SENT){
            pendingMatchings = matchingRepository.findByRequestUserIdAndMatchingStatusOrderByExpiresAtAsc(
                    userId,
                    MatchingStatus.PENDING
            );

            otherMatchings = matchingRepository.findByRequestUserIdAndMatchingStatusInOrderByCreatedAtDesc(
                    userId,
                    List.of(MatchingStatus.CANCELED, MatchingStatus.EXPIRED)
            );
        }else if (matchingBox == MatchingBox.RECEIVED){
            pendingMatchings = matchingRepository.findByTargetUserIdAndMatchingStatusOrderByExpiresAtAsc(
                    userId,
                    MatchingStatus.PENDING
            );

            otherMatchings = matchingRepository.findByTargetUserIdAndMatchingStatusInOrderByCreatedAtDesc(
                    userId,
                    List.of(MatchingStatus.CANCELED, MatchingStatus.EXPIRED)
            );
        }

        List<MatchingResDto.MatchingRes> items = Stream.concat(pendingMatchings.stream(), otherMatchings.stream())
                .map(MatchingConverter::toMatchingResDto)
                .toList();

        return MatchingResDto.MatchingListRes.builder()
                .matchings(items)
                .pendingRequestCount(pendingMatchings.size())
                .build();
    }

    public Matching rejectMatchingRequest(Long matchingId, Long userId) {
        Matching matching = getMatching(matchingId);

        if (!(matching.getTargetUserId().equals(userId))){
            throw new MatchingException(MatchingErrorCode.MATCHING_ACCESS_DENIED);
        }

        if (matching.getMatchingStatus() != MatchingStatus.PENDING){
            throw new MatchingException(MatchingErrorCode.MATCHING_STATUS_NOT_REJECTABLE);
        }

        // TODO: 거절 사유 받는 필드 추가 예정
        matching.changeStatus(MatchingStatus.REJECTED);
        return matching;
    }

    public Matching getMatching(Long matchingId) {
        return matchingRepository.findById(matchingId)
                .orElseThrow(
                        () -> new MatchingException(MatchingErrorCode.MATCHING_NOT_FOUND)
                );
    }

}
