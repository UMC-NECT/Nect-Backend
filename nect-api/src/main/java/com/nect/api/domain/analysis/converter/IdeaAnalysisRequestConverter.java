package com.nect.api.domain.analysis.converter;

import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.util.PromptLoader;
import com.nect.client.openai.dto.OpenAiResponseFormat;
import com.nect.client.openai.dto.OpenAiResponseRequest;
import com.nect.client.openai.dto.OpenAiResponseText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IdeaAnalysisRequestConverter {

    private final PromptLoader promptLoader;
    private final IdeaAnalysisSchemaBuilder ideaAnalysisSchemaBuilder;

    private static final String PROMPT_PATH = "prompts/idea-analysis.txt";

    public OpenAiResponseRequest toOpenAiRequest(IdeaAnalysisRequestDto dto) {

        String prompt = buildPrompt(dto);

        Map<String, Object> schema = ideaAnalysisSchemaBuilder.buildIdeaAnalysisSchema();

        OpenAiResponseFormat format = OpenAiResponseFormat.jsonSchema(
                "IdeaAnalysisResponse",
                schema,
                true
        );

        OpenAiResponseText text = OpenAiResponseText.withFormat(format);

        return OpenAiResponseRequest.builder()
                .model("gpt-4o")
                .input(prompt)
                .text(text)
                .temperature(0.7)
                .maxOutputTokens(4000)
                .metadata(Map.of(
                        "promptVersion", "v1.0",
                        "feature", "idea-analysis"
                ))
                .build();
    }


    private String buildPrompt(IdeaAnalysisRequestDto dto) {
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

        return promptLoader.loadPrompt(PROMPT_PATH, variables);
    }
}