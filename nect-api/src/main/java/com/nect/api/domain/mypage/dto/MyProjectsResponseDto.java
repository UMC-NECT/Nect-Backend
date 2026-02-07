package com.nect.api.domain.mypage.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.user.enums.RoleField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MyProjectsResponseDto {

    private List<ProjectInfo> projects;

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProjectInfo {
        private Long projectId;
        private String projectTitle;
        private String description;
        private LocalDate plannedStartedOn;
        private LocalDate plannedEndedOn;
        private String imageName;

        private List<TeamRoleInfo> teamRoles;
        private LeaderInfo leader;

        private List<TeamMemberProjectInfo> teamMemberProjects;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TeamRoleInfo {
        private RoleField roleField;
        private Integer requiredCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LeaderInfo {
        private Long userId;
        private String name;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TeamMemberProjectInfo {
        private Long projectId;
        private String title;
        private String description;
        private String imageName;
        private LocalDateTime createdAt;
        private LocalDateTime endedAt;
    }

}