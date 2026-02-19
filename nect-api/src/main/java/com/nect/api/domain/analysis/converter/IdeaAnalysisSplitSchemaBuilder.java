package com.nect.api.domain.analysis.converter;

import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 아이디어 분석 파트별 JSON 스키마를 생성하는 빌더입니다.
 *
 * OpenAI의 JSON Schema 응답 형식에 맞게
 * 파트별 구조와 필수 필드를 정의합니다.
 */
@Component
public class IdeaAnalysisSplitSchemaBuilder {

    /**
     * Part A 스키마를 생성합니다.
     */
    public Map<String, Object> buildPartASchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "description", Map.of("type", "string"),
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
                        )
                ),
                "required", List.of("description", "recommended_project_names", "project_duration"),
                "additionalProperties", false
        );
    }

    /**
     * Part B 스키마를 생성합니다.
     */
    public Map<String, Object> buildPartBSchema() {
        List<String> allRoleFields = getRoleFieldNames();
        return Map.of(
                "type", "object",
                "properties", Map.of(
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
                        )
                ),
                "required", List.of("team_composition", "improvement_points"),
                "additionalProperties", false
        );
    }

    /**
     * Part C 스키마를 생성합니다.
     */
    public Map<String, Object> buildPartCSchema() {
        List<String> allRoleFields = getRoleFieldNames();
        return Map.of(
                "type", "object",
                "properties", Map.of(
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
                                                                                "enum", allRoleFields
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
                "required", List.of("weekly_roadmap"),
                "additionalProperties", false
        );
    }

    /**
     * 요청에 허용되는 RoleField 목록을 반환합니다.
     */
    public List<String> getRoleFieldNames() {
        return Arrays.stream(RoleField.values())
                .filter(rf -> rf != RoleField.CUSTOM)
                .filter(rf -> rf.getRole() != Role.OTHER)
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
