package com.nect.api.domain.home.dto;

public record HomeProjectMemberItem(
        Long userId,
        String name,
        String part,
        String roleInPart,
        String matchingStatus,
        String introduction,
        String profileImage
) {
}
