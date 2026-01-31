package com.nect.api.domain.team.chat.dto.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//실시간 채팅 DTO
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatMessageSendRequestDto {
    private Long userId; //TODO 실시간 채팅 테스트용 수정필요
    private String content;
}
