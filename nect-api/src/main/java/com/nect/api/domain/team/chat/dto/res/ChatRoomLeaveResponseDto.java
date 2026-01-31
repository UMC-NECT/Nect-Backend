package com.nect.api.domain.team.chat.dto.res;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
// 방 나가기 DTO
public class ChatRoomLeaveResponseDto {
    private Long roomId;
    private Long userId;
    private String userName;
    private String message;
    private LocalDateTime leftAt;
}
