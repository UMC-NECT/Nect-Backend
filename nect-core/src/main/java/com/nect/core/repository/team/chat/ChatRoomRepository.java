package com.nect.core.repository.team.chat;

import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {


    List<ChatRoom> findByProjectId(Long projectId);

    @Query("SELECT r.id FROM ChatRoom r " +
            "JOIN ChatRoomUser m1 ON r.id = m1.chatRoom.id " +
            "JOIN ChatRoomUser m2 ON r.id = m2.chatRoom.id " +
            "WHERE r.project.id = :projectId " +
            "AND r.type = 'DIRECT' " +
            "AND m1.user.userId = :user1Id " +
            "AND m2.user.userId = :user2Id")
    Optional<Long> findExistingOneOnOneRoomId(
            @Param("projectId") Long projectId,
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );

    List<ChatRoom> findAllByProject_Id(Long projectId);

}
