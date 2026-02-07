package com.nect.api.domain.team.chat.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProjectMemberDto(
        Long userId,
        String nickname,
        String name,
        String profileImage
) {}
