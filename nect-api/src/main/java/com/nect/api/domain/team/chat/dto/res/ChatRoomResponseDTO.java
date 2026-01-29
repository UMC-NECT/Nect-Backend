package com.nect.api.domain.team.chat.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRoomResponseDTO{
    private Long roomId;
    private Long projectId;    //TODO 프로젝트 관련 엔티티에 따라 수정
    private String roomName;
    private ChatRoomType roomType;
    private String profileImage; //TODO : 프로필 엔티티 생기면 수정 필요
    private LocalDateTime createdAt;

}
