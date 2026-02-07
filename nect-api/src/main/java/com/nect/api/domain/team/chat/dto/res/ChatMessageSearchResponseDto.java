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
public class ChatMessageSearchResponseDto {
    private String keyword;
    private Integer totalCount;
    private List<ChatMessageDto> messages;
}