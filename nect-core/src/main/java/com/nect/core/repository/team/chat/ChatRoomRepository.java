package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//전체 채팅방
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 프로젝트의 모든 채팅방 조회
    List<ChatRoom> findByProjectId(Long projectId);

}
