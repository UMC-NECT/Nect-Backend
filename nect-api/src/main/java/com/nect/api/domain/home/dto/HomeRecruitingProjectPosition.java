package com.nect.api.domain.home.dto;

import java.util.List;

public record HomeRecruitingProjectPosition(
        String role,
        Integer requiredCount,
        List<String> description
) {
}
