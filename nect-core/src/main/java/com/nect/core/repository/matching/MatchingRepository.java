package com.nect.core.repository.matching;

import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {

    int countByRequestTypeAndRequestUserIdAndMatchingStatus(
            MatchingRequestType requestType,
            Long requestUserId,
            MatchingStatus status
    );

    int countByRequestTypeAndProjectIdAndFieldIdAndMatchingStatus(
            MatchingRequestType requestType,
            Long projectId,
            Long fieldId,
            MatchingStatus status
    );

    // SENT
    List<Matching> findByRequestUserIdAndMatchingStatusOrderByExpiresAtAsc(
            Long requestUserId,
            MatchingStatus matchingStatus
    );

    List<Matching> findByRequestUserIdAndMatchingStatusInOrderByCreatedAtDesc(
            Long requestUserId,
            List<MatchingStatus> statuses
    );

    //RECEIVED
    List<Matching> findByTargetUserIdAndMatchingStatusOrderByExpiresAtAsc(
            Long targetUserId,
            MatchingStatus matchingStatus
    );

    List<Matching> findByTargetUserIdAndMatchingStatusInOrderByCreatedAtDesc(
            Long targetUserId,
            List<MatchingStatus> statuses
    );
}
