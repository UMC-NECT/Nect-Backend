package com.nect.core.entity.team.chat;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.chat.enums.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//MongoDB 고려중
@Entity
@Table(name="chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 발신자 ID
    @Column(nullable = false)
    private Long senderId;

    // 메시지 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    // 메시지 타입 ( 텍스트,  파일)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    // 공지사항 여부
    @Column(nullable = false)
    private Boolean isPinned = false;


}
