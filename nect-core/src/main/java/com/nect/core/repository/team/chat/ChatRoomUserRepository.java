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

    boolean existsByChatRoomIdAndUserUserId(Long chatRoomId, Long userId);

    int countByChatRoomId(Long chatRoomId);

    Optional<ChatRoomUser> findByChatRoomIdAndUser_UserId(Long chatRoomId, Long userId);

    List<ChatRoomUser> findAllByUserUserId(Long userId);


    @Query("select cru from ChatRoomUser cru " +
            "join fetch cru.chatRoom " +
            "join fetch cru.user " +
            "where cru.chatRoom.id = :roomId and cru.user.userId = :userId")
    Optional<ChatRoomUser> findMemberInRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("SELECT cru FROM ChatRoomUser cru " +
            "JOIN FETCH cru.user " +
            "WHERE cru.chatRoom.id = :roomId AND cru.user.userId = :userId")
    Optional<ChatRoomUser> findByRoomIdAndUserIdWithFetch(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId
    );

    @Query("SELECT cru.user.userId FROM ChatRoomUser cru " +
            "WHERE cru.chatRoom.id = :roomId")
    List<Long> findUserIdsByChatRoomId(@Param("roomId") Long roomId);



}
