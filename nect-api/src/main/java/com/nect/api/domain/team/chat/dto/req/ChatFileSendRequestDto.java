package com.nect.api.domain.team.chat.dto.req;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatFileSendRequestDto {
    private Long userId;//TODO User 엔티티 생성 후 고려사항
    private Long fileId;
}
