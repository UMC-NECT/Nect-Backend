package com.nect.api.domain.team.project.dto;

import lombok.Builder;

@Builder
public record RecruitingProjectResDto(
        Long projectId,
        String title,
        String description
) {}
