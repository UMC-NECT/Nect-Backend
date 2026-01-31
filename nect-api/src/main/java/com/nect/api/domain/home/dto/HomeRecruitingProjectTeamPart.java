package com.nect.api.domain.home.dto;

import java.util.Map;

public record HomeRecruitingProjectTeamPart(
        Integer totalCount,
        Map<String, Integer> parts
) {
}
