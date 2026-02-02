package com.nect.api.domain.home.dto;

import com.nect.core.entity.user.enums.Role;
import lombok.Builder;

@Builder
public record HomeHeaderResponse(

        Long userId,
        String imageUrl,
        String name,
        String email,
        Role role

) {
}
