package com.nect.api.domain.team.chat.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChatRoomResponseDto(
        Long roomId,
        Long projectId,
        String roomName,
        ChatRoomType roomType,
        List<String> profileImages,
        LocalDateTime createdAt
) {}