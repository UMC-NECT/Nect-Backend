package com.nect.api.domain.team.chat.dto.res;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//자신이 속한 채팅방 모음 DTO
public class ChatRoomListDto {
    private Long room_id;
    private String room_name;
    private String last_message;
    private LocalDateTime last_message_time;
    private boolean has_new_message;
    //TODO user관련 프로필 나오면 수정사항
    private String profile_image;

}
