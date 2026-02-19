package com.nect.api.domain.analysis.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto.*;
import com.nect.client.openai.dto.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 파트별 OpenAI 응답을 분석 DTO로 변환하는 컨버터입니다.
 *
 * Part A/B/C 응답을 각각 파싱해 필요한 필드만 추출합니다.
 */
@Component
@RequiredArgsConstructor
public class IdeaAnalysisSplitResponseConverter {

    private final ObjectMapper objectMapper;

    /**
     * Part A 응답을 파싱합니다.
     */
    public IdeaAnalysisResponseDto toPartAResponse(OpenAiResponse openAiResponse) {
        try {
            String jsonContent = openAiResponse.getFirstOutputText();
            JsonNode root = objectMapper.readTree(jsonContent);

            return IdeaAnalysisResponseDto.builder()
                    .recommendedProjectNames(parseRecommendedProjectNames(root))
                    .description(root.get("description").asText())
                    .projectDuration(parseProjectDuration(root))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 응답 파싱 실패 (Part A)", e);
        }
    }

    /**
     * Part B 응답을 파싱합니다.
     */
    public IdeaAnalysisResponseDto toPartBResponse(OpenAiResponse openAiResponse) {
        try {
            String jsonContent = openAiResponse.getFirstOutputText();
            JsonNode root = objectMapper.readTree(jsonContent);

            return IdeaAnalysisResponseDto.builder()
                    .teamComposition(parseTeamComposition(root))
                    .improvementPoints(parseImprovementPoints(root))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 응답 파싱 실패 (Part B)", e);
        }
    }

    /**
     * Part C 응답을 파싱합니다.
     */
    public IdeaAnalysisResponseDto toPartCResponse(OpenAiResponse openAiResponse) {
        try {
            String jsonContent = openAiResponse.getFirstOutputText();
            JsonNode root = objectMapper.readTree(jsonContent);

            return IdeaAnalysisResponseDto.builder()
                    .weeklyRoadmap(parseWeeklyRoadmap(root))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 응답 파싱 실패 (Part C)", e);
        }
    }

    private List<String> parseRecommendedProjectNames(JsonNode root) {
        List<String> names = new ArrayList<>();
        JsonNode namesNode = root.get("recommended_project_names");
        if (namesNode != null && namesNode.isArray()) {
            namesNode.forEach(node -> names.add(node.asText()));
        }
        return names;
    }

    private ProjectDuration parseProjectDuration(JsonNode root) {
        JsonNode durationNode = root.get("project_duration");
        if (durationNode != null) {
            int totalWeeks = durationNode.get("total_weeks").asInt();
            return ProjectDuration.builder()
                    .totalWeeks(totalWeeks)
                    .build();
        }
        return null;
    }

    private List<TeamMember> parseTeamComposition(JsonNode root) {
        List<TeamMember> teamMembers = new ArrayList<>();
        JsonNode teamNode = root.get("team_composition");

        if (teamNode != null && teamNode.isArray()) {
            teamNode.forEach(node -> {
                TeamMember member = TeamMember.builder()
                        .roleField(node.get("role_field").asText())
                        .roleFieldDisplayName(node.get("role_field_display_name").asText())
                        .requiredCount(node.get("count").asInt())
                        .build();
                teamMembers.add(member);
            });
        }
        return teamMembers;
    }

    private List<ImprovementPoint> parseImprovementPoints(JsonNode root) {
        List<ImprovementPoint> points = new ArrayList<>();
        JsonNode pointsNode = root.get("improvement_points");

        if (pointsNode != null && pointsNode.isArray()) {
            pointsNode.forEach(node -> {
                ImprovementPoint point = ImprovementPoint.builder()
                        .order(node.get("order").asInt())
                        .title(node.get("title").asText())
                        .description(node.get("description").asText())
                        .build();
                points.add(point);
            });
        }
        return points;
    }

    private List<WeeklyRoadmap> parseWeeklyRoadmap(JsonNode root) {
        List<WeeklyRoadmap> roadmaps = new ArrayList<>();
        JsonNode roadmapNode = root.get("weekly_roadmap");

        if (roadmapNode != null && roadmapNode.isArray()) {
            roadmapNode.forEach(weekNode -> {
                List<RoleTask> roleTasks = new ArrayList<>();
                JsonNode tasksNode = weekNode.get("role_tasks");

                if (tasksNode != null && tasksNode.isArray()) {
                    tasksNode.forEach(taskNode -> {
                        RoleTask roleTask = RoleTask.builder()
                                .roleField(taskNode.get("role_field").asText())
                                .roleFieldDisplayName(taskNode.get("role_field_display_name").asText())
                                .tasks(taskNode.get("tasks").asText())
                                .build();
                        roleTasks.add(roleTask);
                    });
                }

                WeeklyRoadmap roadmap = WeeklyRoadmap.builder()
                        .weekNumber(weekNode.get("week_number").asInt())
                        .weekTitle(weekNode.get("week_title").asText())
                        .roleTasks(roleTasks)
                        .build();
                roadmaps.add(roadmap);
            });
        }
        return roadmaps;
    }
}
