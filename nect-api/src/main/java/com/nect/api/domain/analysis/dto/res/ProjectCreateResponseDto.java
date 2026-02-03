package com.nect.api.domain.analysis.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProjectCreateResponseDto(
        Long projectId,
        String projectTitle,
        String message
) {
    public static ProjectCreateResponseDto of(Long projectId, String projectTitle) {
        return new ProjectCreateResponseDto(
                projectId,
                projectTitle,
                "프로젝트가 성공적으로 생성되었습니다."
        );
    }
}