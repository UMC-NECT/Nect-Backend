package com.nect.api.global.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OnboardingAnalysisScheme {
    @JsonProperty("profile_type")
    public String profile_type;
    public List<String> tags;
    @JsonProperty("collaboration_style")
    public CollaborationStyle collaboration_style;
    public List<SkillCategory> skills;
    @JsonProperty("role_recommendation")
    public RoleRecommendation role_recommendation;
    @JsonProperty("growth_guide")
    public List<GrowthGuide> growth_guide;
    @JsonProperty("recommended_projects")
    public List<RecommendedProject> recommended_projects;
    @JsonProperty("recommended_team_members")
    public List<RecommendedTeamMember> recommended_team_members;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationStyle {
        public Integer planning;
        public Integer logic;
        public Integer leadership;
        public Integer empathy;
        public Integer execution;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillCategory {
        public String category;
        @JsonProperty("skill_names")
        public List<String> skill_names;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleRecommendation {
        public String leader;
        @JsonProperty("team_member")
        public String team_member;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthGuide {
        public Integer order;
        public String tip;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedProject {
        @JsonProperty("project_name")
        public String project_name;
        public String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedTeamMember {
        public String role;
        public String characteristics;
        public String synergy;
    }
}
