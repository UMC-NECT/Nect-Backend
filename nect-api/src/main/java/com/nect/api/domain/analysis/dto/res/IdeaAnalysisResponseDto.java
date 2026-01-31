
package com.nect.api.domain.analysis.dto.res;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class IdeaAnalysisResponseDto {
    private Long analysisId;

    // 추천하는 프로젝트명
    private List<String> recommendedProjectNames;

    // 예상 기간
    private String estimatedDuration;

    // 프로젝트에 필요한 팀원
    private TeamComposition teamComposition;

    // 보완할 점 3가지
    private String improvementPoint1;
    private String improvementPoint2;
    private String improvementPoint3;

    //  예상 기간 바탕 주차별 로드맵
    private List<WeeklyRoadmap> weeklyRoadmap;

    // 예상 팀원 수 내부 클래스
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TeamComposition {
        private Integer frontend;
        private Integer backend;
        private Integer designer;
        private Integer pm;
        private Integer others;
    }

    // 내부 클래스: 주차별 로드맵
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WeeklyRoadmap {
        private Integer weekNumber;
        private String weekTitle;

        // 드롭다운 펼쳤을 때 보이는 내용
        private String pmTasks;
        private String designTasks;
        private String frontendTasks;
        private String backendTasks;
    }

}