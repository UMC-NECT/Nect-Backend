package com.nect.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long roomId;           // 채팅방 ID
    private Long senderId;         // 발신자 ID
    private String senderName;     // 발신자 이름 
    private String content;        // 메시지 내용
    private String messageType;    // 메시지 타입
    private Boolean isPinned;      // 공지 여부
    private LocalDateTime createdAt;  // 생성 시간

    // 채팅 메시지가 파일일 때
    private String fileName;
    private String fileUrl;
    
}
