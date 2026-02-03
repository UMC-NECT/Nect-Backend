package com.nect.api.domain.team.chat.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomInviteRequestDto {
    private List<Long> targetUserIds;
}
