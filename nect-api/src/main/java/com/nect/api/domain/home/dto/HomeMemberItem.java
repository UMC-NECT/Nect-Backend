package com.nect.api.domain.home.dto;

import java.util.List;

public record HomeMemberItem(
        Long userId,
        String imageUrl,
        String name,
        String part,
        String introduction,
        String status,
        Boolean isScrapped,
        List<String> roles
) {
}
