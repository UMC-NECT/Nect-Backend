package com.nect.api.domain.analysis.converter;

import com.nect.core.entity.user.enums.RoleField;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class IdeaAnalysisSchemaBuilder {

    public Map<String, Object> buildIdeaAnalysisSchema() {

        List<String> allRoleFields = Arrays.stream(RoleField.values())
                .filter(rf -> rf != RoleField.CUSTOM)
                .map(Enum::name)
                .collect(Collectors.toList());

        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "recommended_project_names", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "project_duration", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "total_weeks", Map.of("type", "integer")
                                ),
                                "required", List.of("total_weeks"),
                                "additionalProperties", false
                        ),
                        "team_composition", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "role_field", Map.of(
                                                        "type", "string",
                                                        "enum", allRoleFields 
                                                ),
                                                "role_field_display_name", Map.of("type", "string"),
                                                "count", Map.of("type", "integer")
                                        ),
                                        "required", List.of("role_field", "role_field_display_name", "count"),
                                        "additionalProperties", false
                                )
                        ),
                        "improvement_points", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "order", Map.of("type", "integer"),
                                                "title", Map.of("type", "string"),
                                                "description", Map.of("type", "string")
                                        ),
                                        "required", List.of("order", "title", "description"),
                                        "additionalProperties", false
                                )
                        ),
                        "weekly_roadmap", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "week_number", Map.of("type", "integer"),
                                                "week_title", Map.of("type", "string"),
                                                "role_tasks", Map.of(
                                                        "type", "array",
                                                        "items", Map.of(
                                                                "type", "object",
                                                                "properties", Map.of(
                                                                        "role_field", Map.of(
                                                                                "type", "string",
                                                                                "enum", allRoleFields  // ✅ 여기도 동일하게
                                                                        ),
                                                                        "role_field_display_name", Map.of("type", "string"),
                                                                        "tasks", Map.of("type", "string")
                                                                ),
                                                                "required", List.of("role_field", "role_field_display_name", "tasks"),
                                                                "additionalProperties", false
                                                        )
                                                )
                                        ),
                                        "required", List.of("week_number", "week_title", "role_tasks"),
                                        "additionalProperties", false
                                )
                        )
                ),
                "required", List.of(
                        "recommended_project_names",
                        "project_duration",
                        "team_composition",
                        "improvement_points",
                        "weekly_roadmap"
                ),
                "additionalProperties", false
        );
    }
}
