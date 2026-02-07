package com.nect.core.entity.team.chat;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_file")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    //R2에 저장된 파일명
    @Column(nullable = false, length = 500)
    private String storedFileName;


    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private Long fileSize;

    // 파일 타입 (image/jpeg, application/pdf 등)
    @Column(nullable = false, length = 100)
    private String fileType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id",nullable = true)
    private ChatMessage chatMessage;

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public void updateFileUrl(String newFileUrl) {
        this.fileUrl = newFileUrl;
    }

}