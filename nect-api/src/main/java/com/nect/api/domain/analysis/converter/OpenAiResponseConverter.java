package com.nect.api.domain.analysis.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.client.openai.dto.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OpenAiResponseConverter {

    private final ObjectMapper objectMapper;


      //OpenAiResponse → IdeaAnalysisResponseDto

    public IdeaAnalysisResponseDto toIdeaAnalysisResponse(OpenAiResponse openAiResponse) {
        // 텍스트 추출
        String output = openAiResponse.getFirstOutputText();
        if (output == null || output.isBlank()) {
            throw new RuntimeException("OpenAI 응답이 비어있습니다.");
        }


        // JSON 파싱
        return parseJson(output);
    }

    /**
     * JSON 문자열 파싱
     */
    private IdeaAnalysisResponseDto parseJson(String output) {
        try {
            // JSON Schema로 응답받으므로 마크다운 블록 제거
            String cleaned = cleanJsonString(output);

            // IdeaAnalysisResponseDto로 파싱
            IdeaAnalysisResponseDto response = objectMapper.readValue(
                    cleaned,
                    IdeaAnalysisResponseDto.class
            );

            return response;

        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 문자열 정리
     */
    private String cleanJsonString(String jsonString) {
        String cleaned = jsonString.trim();

        // 마크다운 코드 블록 제거
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }
}