package com.nect.api.domain.team.chat.dto.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDto;
import com.nect.core.entity.team.chat.enums.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//채팅 메시지 DTO
public class ChatMessageDto {
    private Long messageId;
    private Long userId;
    private Long roomId;
    private String userName;


    private String profileImage;

    private String content;
    private MessageType messageType;
    private Boolean isPinned;
    private LocalDateTime createdAt;

    private Integer readCount;

    private ChatFileUploadResponseDto fileInfo;

}
