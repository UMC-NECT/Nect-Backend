package com.nect.api.domain.analysis.service;

import com.nect.api.domain.analysis.converter.OpenAiRequestConverter;
import com.nect.api.domain.analysis.converter.OpenAiResponseConverter;

import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.global.ai.exception.OpenAiException;
import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.dto.OpenAiResponse;
import com.nect.client.openai.dto.OpenAiResponseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.nect.api.global.ai.enums.OpenAiExceptionCode.OPENAI_REQUEST_FAILED;


@Service
@RequiredArgsConstructor
public class IdeaAnalysisService {

    private final OpenAiClient openAiClient;
    private final OpenAiRequestConverter requestConverter;
    private final OpenAiResponseConverter responseConverter;

    /**
     * 프로젝트 아이디어 분석
     */
    public IdeaAnalysisResponseDto analyzeProjectIdea(Long userId, IdeaAnalysisRequestDto requestDto) {

        try {
            //  OpenAI 요청 생성
            OpenAiResponseRequest openAiRequest = requestConverter.toOpenAiRequest(requestDto);

            // OpenAI API 호출
            OpenAiResponse openAiResponse = openAiClient.createResponse(openAiRequest);

            //TODO : DB에 따로 저장할 필요가 있는지 PM님에게 여쭤보고 수정
            IdeaAnalysisResponseDto response = responseConverter.toIdeaAnalysisResponse(openAiResponse);

            return response;

        } catch (Exception e) {
            throw new OpenAiException(OPENAI_REQUEST_FAILED, "AI 분석 중 예기치 못한 오류가 발생했습니다.", e);        }
    }
}