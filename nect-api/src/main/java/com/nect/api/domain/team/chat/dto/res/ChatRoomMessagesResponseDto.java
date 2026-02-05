package com.nect.api.domain.team.chat.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRoomMessagesResponseDto {


    private Long roomId;
    private String roomName;
    private Integer memberCount;

    private List<ChatMessageDto> messages;


    private Boolean hasNext;
}