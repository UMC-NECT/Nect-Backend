package com.nect.api.domain.home.dto;

import java.util.List;

public record HomeMembersResponse(
        List<HomeMemberItem> members
) {
}
