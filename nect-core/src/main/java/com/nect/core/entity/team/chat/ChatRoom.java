package com.nect.core.entity.team.chat;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="chat_room")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Project 엔티티 생성 후 @ManyToOne으로 변경 필요
    @Column(nullable = false)
    private long projectId;

    // 1:1 채팅방일경우 팀원의 이름
    @Column(length=100)
    private String name;

    // 팀채팅 or 1:1 채팅
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private ChatRoomType type;


}
