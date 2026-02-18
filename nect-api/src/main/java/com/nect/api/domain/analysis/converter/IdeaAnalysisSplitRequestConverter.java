package com.nect.api.domain.analysis.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.util.PromptLoader;
import com.nect.client.openai.config.OpenAiProperties;
import com.nect.client.openai.dto.OpenAiResponseFormat;
import com.nect.client.openai.dto.OpenAiResponseRequest;
import com.nect.client.openai.dto.OpenAiResponseText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 아이디어 분석을 파트별 요청으로 변환하는 컨버터입니다.
 *
 * 프롬프트 템플릿과 JSON 스키마를 결합하여
 * OpenAI 요청 객체를 생성합니다.
 */
@Component
@RequiredArgsConstructor
public class IdeaAnalysisSplitRequestConverter {

    private static final String PROMPT_PATH_A = "prompts/idea-analysis-a.txt";
    private static final String PROMPT_PATH_B = "prompts/idea-analysis-b.txt";
    private static final String PROMPT_PATH_C = "prompts/idea-analysis-c.txt";

    private static final int PART_A_MAX_OUTPUT_TOKENS = 800;
    private static final int PART_B_MAX_OUTPUT_TOKENS = 2000;
    private static final int PART_C_MAX_OUTPUT_TOKENS = 2500;

    private final PromptLoader promptLoader;
    private final IdeaAnalysisSplitSchemaBuilder splitSchemaBuilder;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    /**
     * Part A(설명, 추천 프로젝트명, 기간) 요청을 생성합니다.
     */
    public OpenAiResponseRequest toOpenAiRequestPartA(IdeaAnalysisRequestDto dto) {
        String prompt = buildBasePrompt(PROMPT_PATH_A, dto, Map.of());

        OpenAiResponseFormat format = OpenAiResponseFormat.jsonSchema(
                "IdeaAnalysisPartA",
                splitSchemaBuilder.buildPartASchema(),
                true
        );

        return OpenAiResponseRequest.builder()
                .model(openAiProperties.getModel())
                .input(prompt)
                .text(OpenAiResponseText.withFormat(format))
                .temperature(0.7)
                .maxOutputTokens(PART_A_MAX_OUTPUT_TOKENS)
                .metadata(Map.of(
                        "promptVersion", "v1.0",
                        "feature", "idea-analysis-part-a"
                ))
                .build();
    }

    /**
     * Part B(팀 구성, 개선점) 요청을 생성합니다.
     */
    public OpenAiResponseRequest toOpenAiRequestPartB(IdeaAnalysisRequestDto dto) {
        Map<String, String> extra = Map.of(
                "roleFields", String.join(", ", splitSchemaBuilder.getRoleFieldNames())
        );
        String prompt = buildBasePrompt(PROMPT_PATH_B, dto, extra);

        OpenAiResponseFormat format = OpenAiResponseFormat.jsonSchema(
                "IdeaAnalysisPartB",
                splitSchemaBuilder.buildPartBSchema(),
                true
        );

        return OpenAiResponseRequest.builder()
                .model(openAiProperties.getModel())
                .input(prompt)
                .text(OpenAiResponseText.withFormat(format))
                .temperature(0.7)
                .maxOutputTokens(PART_B_MAX_OUTPUT_TOKENS)
                .metadata(Map.of(
                        "promptVersion", "v1.0",
                        "feature", "idea-analysis-part-b"
                ))
                .build();
    }

    /**
     * Part C(주차별 로드맵) 요청을 생성합니다.
     *
     * 주차 범위와 팀 구성을 함께 전달합니다.
     */
    public OpenAiResponseRequest toOpenAiRequestPartC(
            IdeaAnalysisRequestDto dto,
            int totalWeeks,
            int startWeek,
            int endWeek,
            List<IdeaAnalysisResponseDto.TeamMember> teamComposition) {

        Map<String, String> extra = new HashMap<>();
        extra.put("totalWeeks", String.valueOf(totalWeeks));
        extra.put("startWeek", String.valueOf(startWeek));
        extra.put("endWeek", String.valueOf(endWeek));
        extra.put("teamComposition", toTeamCompositionJson(teamComposition));

        String prompt = buildBasePrompt(PROMPT_PATH_C, dto, extra);

        OpenAiResponseFormat format = OpenAiResponseFormat.jsonSchema(
                "IdeaAnalysisPartC",
                splitSchemaBuilder.buildPartCSchema(),
                true
        );

        return OpenAiResponseRequest.builder()
                .model(openAiProperties.getModel())
                .input(prompt)
                .text(OpenAiResponseText.withFormat(format))
                .temperature(0.7)
                .maxOutputTokens(PART_C_MAX_OUTPUT_TOKENS)
                .metadata(Map.of(
                        "promptVersion", "v1.0",
                        "feature", "idea-analysis-part-c"
                ))
                .build();
    }

    /**
     * 공통 프롬프트 템플릿을 로드하고 변수를 치환합니다.
     */
    private String buildBasePrompt(String path, IdeaAnalysisRequestDto dto, Map<String, String> extra) {
        Map<String, String> variables = new HashMap<>();
        variables.put("projectName", dto.getProjectName());
        variables.put("projectSummary", dto.getProjectSummary());
        variables.put("targetUsers", dto.getTargetUsers());
        variables.put("problemStatement", dto.getProblemStatement());
        variables.put("coreFeature1", dto.getCoreFeature1());
        variables.put("coreFeature2", dto.getCoreFeature2());
        variables.put("coreFeature3", dto.getCoreFeature3());
        variables.put("platform", dto.getPlatform());
        variables.put("referenceServices", dto.getReferenceServices());
        variables.put("technicalChallenges", dto.getTechnicalChallenges());
        variables.put("targetCompletionDate",
                dto.getTargetCompletionDate() != null ? dto.getTargetCompletionDate().toString() : "미정");
        variables.put("today", LocalDate.now().toString());
        variables.putAll(extra);

        return promptLoader.loadPrompt(path, variables);
    }

    /**
     * 팀 구성 정보를 JSON 문자열로 변환합니다.
     */
    private String toTeamCompositionJson(List<IdeaAnalysisResponseDto.TeamMember> teamComposition) {
        if (teamComposition == null || teamComposition.isEmpty()) {
            return "[]";
        }
        List<Map<String, Object>> items = teamComposition.stream()
                .map(member -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("role_field", member.getRoleField());
                    item.put("role_field_display_name", member.getRoleFieldDisplayName());
                    item.put("count", member.getRequiredCount());
                    return item;
                })
                .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
