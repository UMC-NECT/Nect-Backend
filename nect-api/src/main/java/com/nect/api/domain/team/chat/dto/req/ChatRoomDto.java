package com.nect.api.domain.team.chat.dto.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//자신이 속한 채팅방 1개 DTO
public class ChatRoomDto {
    private Long roomId;
    private Long projectId;
    private String name;
    private ChatRoomType type;
    private List<Long> userIds;
    private Boolean hasNewMessage; // 새 메시지 여부
    private ChatMessageDto lastMessage; // 마지막 메시지
    private LocalDateTime createdAt;
}
