package com.nect.api.domain.team.chat.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatNotificationResponseDto {
    private Long roomId;
    private Boolean isNotificationEnabled;
}