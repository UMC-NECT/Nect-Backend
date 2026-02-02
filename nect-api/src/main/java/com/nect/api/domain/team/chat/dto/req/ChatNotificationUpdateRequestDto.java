package com.nect.api.domain.team.chat.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatNotificationUpdateRequestDto {
    private Boolean isNotificationEnabled;
}