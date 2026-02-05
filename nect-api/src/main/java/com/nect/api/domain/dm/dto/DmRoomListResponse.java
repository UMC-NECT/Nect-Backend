package com.nect.api.domain.dm.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DmRoomListResponse(
        List<DmRoomSummaryDto> rooms,
        Long nextCursor
) {
}
