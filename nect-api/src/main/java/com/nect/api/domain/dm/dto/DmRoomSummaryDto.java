package com.nect.api.domain.dm.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DmRoomSummaryDto(
        Long otherUserId,
        Long lastMessageId,
        String lastMessage,
        LocalDateTime lastMessageAt,
        Long unreadCount
) {
}
