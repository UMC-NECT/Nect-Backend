package com.nect.api.domain.analysis.converter;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
@Component
public class JsonSchemaBuilder {

    public Map<String, Object> buildIdeaAnalysisSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");


        schema.put("required", Arrays.asList(
                "recommended_project_names",
                "estimated_duration",
                "team_composition",
                "improvement_point1",
                "improvement_point2",
                "improvement_point3",
                "weekly_roadmap"
        ));

        Map<String, Object> properties = new HashMap<>();

        properties.put("recommended_project_names", Map.of(
                "type", "array",
                "items", Map.of("type", "string")
        ));

        properties.put("estimated_duration", Map.of("type", "string"));

        Map<String, Object> teamComposition = new HashMap<>();
        teamComposition.put("type", "object");
        teamComposition.put("required", Arrays.asList("frontend", "backend", "designer", "pm", "others"));
        teamComposition.put("properties", Map.of(
                "frontend", Map.of("type", "integer"),
                "backend", Map.of("type", "integer"),
                "designer", Map.of("type", "integer"),
                "pm", Map.of("type", "integer"),
                "others", Map.of("type", "integer")
        ));
        teamComposition.put("additionalProperties", false);
        properties.put("team_composition", teamComposition);

        properties.put("improvement_point1", Map.of("type", "string"));
        properties.put("improvement_point2", Map.of("type", "string"));
        properties.put("improvement_point3", Map.of("type", "string"));

        Map<String, Object> roadmapItem = new HashMap<>();
        roadmapItem.put("type", "object");
        roadmapItem.put("required", Arrays.asList("week_number", "week_title", "pm_tasks",
                "design_tasks", "frontend_tasks", "backend_tasks"));
        roadmapItem.put("properties", Map.of(
                "week_number", Map.of("type", "integer"),
                "week_title", Map.of("type", "string"),
                "pm_tasks", Map.of("type", "string"),
                "design_tasks", Map.of("type", "string"),
                "frontend_tasks", Map.of("type", "string"),
                "backend_tasks", Map.of("type", "string")
        ));
        roadmapItem.put("additionalProperties", false);

        properties.put("weekly_roadmap", Map.of(
                "type", "array",
                "items", roadmapItem
        ));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);

        return schema;
    }
}