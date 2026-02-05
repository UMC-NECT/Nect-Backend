package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatFileRepository extends JpaRepository<ChatFile, Long> {

    Optional<ChatFile> findByChatMessageId(Long messageId);

    List<ChatFile> findAllByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(Long chatRoomId, LocalDateTime threshold);

    List<ChatFile> findAllByCreatedAtBefore(LocalDateTime threshold);


    @Query("SELECT cf FROM ChatFile cf " +
            "WHERE cf.chatRoom.id = :roomId " +
            "AND cf.createdAt > :after " +
            "ORDER BY cf.createdAt DESC")
    List<ChatFile> findTopNByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(
            @Param("roomId") Long roomId,
            @Param("after") LocalDateTime after,
            Pageable pageable);

    @Query("SELECT COUNT(cf) FROM ChatFile cf " +
            "WHERE cf.chatRoom.id = :roomId " +
            "AND cf.createdAt > :after")
    int countByChatRoomIdAndCreatedAtAfter(
            @Param("roomId") Long roomId,
            @Param("after") LocalDateTime after);

}
