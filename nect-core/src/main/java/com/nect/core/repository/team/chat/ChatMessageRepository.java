package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {

    @Query("SELECT cm FROM ChatMessage cm " +
            "JOIN FETCH cm.user " +
            "WHERE cm.chatRoom = :chatRoom " +
            "ORDER BY cm.id DESC")
    List<ChatMessage> findByChatRoomOrderByIdDesc(@Param("chatRoom") ChatRoom chatRoom, Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm " +
            "JOIN FETCH cm.user " +
            "WHERE cm.chatRoom = :chatRoom " +
            "AND cm.id < :id " +
            "ORDER BY cm.id DESC")
    List<ChatMessage> findByChatRoomAndIdLessThanOrderByIdDesc(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("id") Long id,
            Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :roomId " +
            "AND cm.id < :lastMessageId " +
            "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByRoomIdBeforeMessageId(
            @Param("roomId") Long roomId,
            @Param("lastMessageId") Long lastMessageId,
            Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :roomId " +
            "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessages(
            @Param("roomId") Long roomId,
            Pageable pageable);

    Optional<ChatMessage> findByChatRoomIdAndUserUserId(Long chatRoomId, Long userId);

    int countByChatRoomId(Long chatRoomId);

    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :chatRoomId " +
            "ORDER BY cm.createdAt DESC " +
            "LIMIT 1")
    Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(@Param("chatRoomId") Long chatRoomId);

    Optional<ChatMessage> findTopByChatRoomOrderByIdDesc(ChatRoom chatRoom);

    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :roomId " +
            "AND cm.messageType = 'TEXT' " +
            "AND cm.content LIKE %:keyword% " +
            "ORDER BY cm.createdAt DESC")
    Page<ChatMessage> searchByKeyword(
            @Param("roomId") Long roomId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<ChatMessage> findAllByChatRoomIdAndIsPinnedTrue(Long chatRoomId);
}

