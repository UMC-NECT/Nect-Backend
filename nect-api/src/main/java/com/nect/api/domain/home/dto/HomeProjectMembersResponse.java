package com.nect.api.domain.home.dto;

public record HomeProjectMembersResponse(
        Long projectId,
        HomeProjectTeamMembers teamMembers
) {
}
