package com.nect.api.domain.analysis.dto.res;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class IdeaAnalysisResponseDto {
    private Long analysisId;


    private List<String> recommendedProjectNames;

    private ProjectDuration projectDuration;

    private List<TeamMember> teamComposition;

    private List<ImprovementPoint> improvementPoints;

    private List<WeeklyRoadmap> weeklyRoadmap;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProjectDuration {
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalWeeks;
        private String displayText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TeamMember {
        private String roleField;
        private String roleFieldDisplayName;
        private Integer requiredCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ImprovementPoint {
        private Integer order;
        private String title;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WeeklyRoadmap {
        private Integer weekNumber;
        private String weekTitle;
        private LocalDate weekStartDate;
        private LocalDate weekEndDate;
        private String weekPeriod;

        private List<RoleTask> roleTasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RoleTask {
        private String roleField;
        private String roleFieldDisplayName;
        private String tasks;
    }
}