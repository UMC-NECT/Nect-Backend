package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser,Long> {

    // 사용자의 특정 채팅방 멤버 정보 조회
    Optional<ChatRoomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    // 사용자가 속한 모든 채팅방 조회
    List<ChatRoomUser> findByUserId(Long userId);

    List<ChatRoomUser> findAllByUserId(Long userId);


    @Query("select cru from ChatRoomUser cru " +
            "join fetch cru.chatRoom " +
            "join fetch cru.user " +
            "where cru.chatRoom.id = :roomId and cru.user.id = :userId")
    Optional<ChatRoomUser> findMemberInRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

}
