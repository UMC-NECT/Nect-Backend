package com.nect.api.domain.home.dto;

import java.util.List;
import java.util.Map;

public record HomeProjectTeamMembers(
        Map<String, List<HomeProjectMemberItem>> parts
) {
}
