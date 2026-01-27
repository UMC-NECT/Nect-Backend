package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatFileRepository extends JpaRepository<ChatFile, Long> {

    // 메시지의 파일 정보 조회
    Optional<ChatFile> findByMessageId(Long messageId);
}
    Optional<ChatFile> findByChatMessageId(Long messageId);}
