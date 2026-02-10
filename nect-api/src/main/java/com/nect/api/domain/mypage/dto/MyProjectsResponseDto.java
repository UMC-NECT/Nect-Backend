package com.nect.api.domain.mypage.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.team.ProjectInterest;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.enums.PlanFileType;
import com.nect.core.entity.user.enums.RoleField;
import lombok.*;

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

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProjectFieldResponse{

        Long projectId;
        private List<InterestInfo> fields;

        public static ProjectFieldResponse ofProject(Long projectId, List<ProjectInterest> interests) {

            List<InterestInfo> fields = interests.stream()
                    .map(interest -> new InterestInfo(interest.getInterestField().getDescription(), interest.getSelected()))
                    .toList();

            return new ProjectFieldResponse(projectId, fields);
        }

    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    private static class InterestInfo{
        String fieldName;
        Boolean isSelected;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class StringListResponse {
        Long projectId;
        List<String> values;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProjectPlanFileInfo {
        private Long planFileId;
        private String name;
        private String fileName;
        private PlanFileType planFileType;
        private FileExt fileExt;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProjectPlanFilesResponse {
        private Long projectId;
        private List<ProjectPlanFileInfo> files;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProjectPlanFileDownloadResponse {
        private String downloadUrl;
    }

}