package com.nect.api.domain.home.dto;

public record HomeRecruitingProjectLeader(
        Long userId,
        String name,
        String role,
        String introduction,
        String profileImage
) {
}
