package com.nect.api.domain.team.chat.dto.res;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChatRoomInviteResponseDto(
        Long roomId,
        Integer invitedCount,
        List<String> invitedUserNames,
        List<String> profileImages
) {}