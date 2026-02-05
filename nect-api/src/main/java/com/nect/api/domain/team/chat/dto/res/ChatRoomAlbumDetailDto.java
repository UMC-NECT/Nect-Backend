package com.nect.api.domain.team.chat.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChatRoomAlbumDetailDto(
        Long roomId,
        String roomName,
        List<ChatFileResponseDto> files,
        Integer totalCount,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext
) {}