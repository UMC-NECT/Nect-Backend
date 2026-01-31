package com.nect.api.domain.team.chat.dto.req;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//1:1 방 생성 DTO
public class ChatRoomCreateRequestDto {
    private Long project_id; //TODO 프로젝트  엔티티 받으면 수정필요
    private Long target_user_id;
}

