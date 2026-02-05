package com.nect.core.repository.matching;

import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {

    int countByRequestTypeAndRequestUserAndMatchingStatus(
            MatchingRequestType requestType,
            User requestUser,
            MatchingStatus status
    );

    int countByRequestTypeAndProjectAndFieldAndMatchingStatus(
            MatchingRequestType requestType,
            Project project,
            RoleField field,
            MatchingStatus status
    );

    // SENT
    List<Matching> findByRequestUserAndMatchingStatusOrderByExpiresAtAsc(
            User requestUser,
            MatchingStatus matchingStatus
    );

    List<Matching> findByRequestUserAndMatchingStatusInOrderByCreatedAtDesc(
            User requestUser,
            List<MatchingStatus> statuses
    );

    //RECEIVED
    List<Matching> findByTargetUserAndMatchingStatusOrderByExpiresAtAsc(
            User targetUser,
            MatchingStatus matchingStatus
    );

    List<Matching> findByTargetUserAndMatchingStatusInOrderByCreatedAtDesc(
            User targetUser,
            List<MatchingStatus> statuses
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            update Matching m
            set m.matchingStatus = com.nect.core.entity.matching.enums.MatchingStatus.EXPIRED
            where m.matchingStatus = com.nect.core.entity.matching.enums.MatchingStatus.PENDING
                        and m.expiresAt <= :now
            """)
    int bulkExpire(@Param("now") LocalDateTime now);

    int countByRequestUserAndMatchingStatus(
            User requestUser,
            MatchingStatus matchingStatus
    );

    int countByTargetUserAndMatchingStatus(
            User targetUser,
            MatchingStatus matchingStatus
    );

    @Query("""
            select m
            from Matching m
            where m.matchingStatus = :matchingStatus
                and m.requestType = :matchingRequestType
                and m.requestUser = :user
            order by m.expiresAt asc
    """)
    List<Matching> findSentMatchingsOrderByExpiresAt(
            @Param("matchingRequestType") MatchingRequestType matchingRequestType,
            @Param("user") User user,
            @Param("matchingStatus") MatchingStatus matchingStatus
    );

    @Query("""
            select m
            from Matching m
            where m.matchingStatus = :matchingStatus
                and m.targetUser = :user
                and m.requestType = :matchingRequestType
            order by m.expiresAt asc
    """)
    List<Matching> findReceivedMatchingsOrderByExpiresAt(
            @Param("matchingRequestType") MatchingRequestType matchingRequestType,
            @Param("user") User user,
            @Param("matchingStatus") MatchingStatus matchingStatus
    );
}
