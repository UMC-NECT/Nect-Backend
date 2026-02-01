package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatFileRepository extends JpaRepository<ChatFile, Long> {

    // 메시지의 파일 정보 조회
    Optional<ChatFile> findByChatMessageId(Long messageId);

    // 특정 채팅방에서 15일 이내 파일만 조회
    List<ChatFile> findAllByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(Long chatRoomId, LocalDateTime threshold);


    //15일이 지난 만료된 파일들 찾기
    List<ChatFile> findAllByCreatedAtBefore(LocalDateTime threshold);
}
