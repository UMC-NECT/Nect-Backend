package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {

    List<ChatMessage> findByChatRoomOrderByIdDesc(ChatRoom chatRoom, Pageable pageable);

    List<ChatMessage> findByChatRoomAndIdLessThanOrderByIdDesc(ChatRoom chatRoom, Long id, Pageable pageable);


    //TODO 새 메시지 확인
    List<ChatMessage> findByChatRoomAndIdGreaterThanOrderByIdAsc(
            ChatRoom chatRoom,
            Long lastMessageId
    );
     //TODO 채팅방의 전체 메시지 수
    long countByChatRoom(ChatRoom chatRoom);

     // TODO 특정 시간 이후 메시지 수 (읽지 않은 메시지 카운트)
    long countByChatRoomAndCreatedAtAfter(ChatRoom chatRoom, LocalDateTime after);
     // TODO 고정된 메시지 조회
    List<ChatMessage> findByChatRoomAndIsPinnedTrueOrderByCreatedAtDesc(ChatRoom chatRoom);

    Optional<ChatMessage> findTopByChatRoomOrderByIdDesc(ChatRoom chatRoom);

}

