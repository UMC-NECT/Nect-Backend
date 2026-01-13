package com.nect.core.entity.team.chat;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 ID
    @Column(nullable = false)
    private Long chatRoomId;

    // 사용자 ID
    @Column(nullable = false)
    private Long userId;

    // 마지막으로 읽은 시간
    private LocalDateTime lastReadAt;

}
