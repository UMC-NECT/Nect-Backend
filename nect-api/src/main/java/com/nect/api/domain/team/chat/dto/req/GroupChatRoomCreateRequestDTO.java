package com.nect.api.domain.team.chat.dto.req;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
// 팀 채팅 방 생성 DTO
public class GroupChatRoomCreateRequestDTO {

    private Long projectId;//TODO 프로젝트 관련 엔티티 받으면 수정필요
    private String roomName;
    private List<Long> targetUserIds;

}
