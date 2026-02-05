package com.nect.api.domain.team.chat.dto.res;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;


import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChatRoomListDto(
        Long roomId,
        String roomName,
        Integer memberCount,

        String lastMessage,
        LocalDateTime lastMessageTime,

        // TODO: 멤버들의 프로필 사진 목록 (최대 4개)
        List<String> profileImages,

        // 새 메시지 여부
        Boolean hasNewMessage
) {}