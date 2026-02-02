package com.nect.core.entity.team.chat;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.chat.enums.MessageType;
import com.nect.core.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

//MongoDB 고려중
@Entity
@Table(name="chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // 메시지 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    // 메시지 타입 ( 텍스트,  파일)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    // 공지사항 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    // TODO 발신자 (User 외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;


}
