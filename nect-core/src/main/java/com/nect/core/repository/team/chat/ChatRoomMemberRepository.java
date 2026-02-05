package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//채팅방별 멤버
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember,Long> {


    // 사용자가 속한 모든 채팅방 조회
    List<ChatRoomMember> findByUserId(Long userId);


}
