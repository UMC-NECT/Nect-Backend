package com.nect.core.repository.dm;

import com.nect.core.entity.dm.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DmRepository extends JpaRepository<DirectMessage, Long> {

    interface UnreadCountRow {
        Long getSenderId();
        Long getUnreadCount();
    }

    @Query("""
        SELECT dm FROM DirectMessage dm
        WHERE ((dm.sender.userId = :userA AND dm.receiver.userId = :userB)
            OR (dm.sender.userId = :userB AND dm.receiver.userId = :userA))
          AND (:cursor IS NULL OR dm.id < :cursor)
        ORDER BY dm.id DESC
    """)
    List<DirectMessage> findConversation(
            @Param("userA") Long userA,
            @Param("userB") Long userB,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT dm FROM DirectMessage dm
        WHERE dm.id IN (
            SELECT MAX(dm2.id) FROM DirectMessage dm2
            WHERE (dm2.sender.userId = :userId OR dm2.receiver.userId = :userId)
            GROUP BY CASE
                WHEN dm2.sender.userId = :userId THEN dm2.receiver.userId
                ELSE dm2.sender.userId
            END
        )
          AND (:cursor IS NULL OR dm.id < :cursor)
        ORDER BY dm.id DESC
    """)
    List<DirectMessage> findLatestMessagesByUser(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT dm.sender.userId as senderId, COUNT(dm) as unreadCount
        FROM DirectMessage dm
        WHERE dm.receiver.userId = :receiverId
          AND dm.isRead = false
        GROUP BY dm.sender.userId
    """)
    List<UnreadCountRow> countUnreadBySender(@Param("receiverId") Long receiverId);

    @Modifying
    @Query("""
        UPDATE DirectMessage dm
        SET dm.isRead = true
        WHERE dm.receiver.userId = :receiverId
          AND dm.sender.userId = :senderId
          AND dm.isRead = false
          AND (:lastReadId IS NULL OR dm.id <= :lastReadId)
    """)
    int markAsRead(
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId,
            @Param("lastReadId") Long lastReadId
    );
}
