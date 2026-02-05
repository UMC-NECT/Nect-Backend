package com.nect.api.domain.dm.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DmMessageListResponse(
        List<DirectMessageDto> messages,
        Long nextCursor
) {
}
