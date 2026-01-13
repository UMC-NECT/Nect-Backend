package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//채팅방 내부
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    //채팅방 메시지 조회
    //내림차순 조회

    
    //공지사항 조회

    
    
}
