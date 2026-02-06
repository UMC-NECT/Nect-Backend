package com.nect.api.domain.home.dto;

import java.util.Map;

public record HomeProjectItem(
    Long projectId,
    String imageUrl,
    String projectName,
    String authorName,
    String authorPart,
    String introduction,
    Integer leftDays,
    Integer maxMemberCount,
    Integer curMemberCount,
    Boolean isScrapped,
    String status,
    Map<String, Integer> roles
) {
}
