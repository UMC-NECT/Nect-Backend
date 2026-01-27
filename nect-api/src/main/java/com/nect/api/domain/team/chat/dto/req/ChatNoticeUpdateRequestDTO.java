package com.nect.api.domain.team.chat.dto.req;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//공지사항 등록 DTO
public class ChatNoticeUpdateRequestDTO {
    private Boolean isPinned;
}
