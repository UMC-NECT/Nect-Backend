package com.nect.api.domain.team.chat.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.team.chat.enums.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//공지사항 등록 DTO
public class ChatNoticeResponseDTO {
    private Long messageId;
    private Long roomId;
    private String content;
    private MessageType messageType;
    private String senderName;
    private Boolean isPinned;
    private LocalDateTime registeredAt;

}
