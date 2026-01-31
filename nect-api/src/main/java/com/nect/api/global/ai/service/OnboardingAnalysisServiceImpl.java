package com.nect.api.global.ai.service;

import com.nect.api.global.ai.dto.OnboardingAnalysisScheme;
import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.dto.OpenAiResponse;
import org.springframework.stereotype.Service;

/**
 * 온보딩 분석 요청을 OpenAI로 전달하고
 * 결과를 반환하는 서비스입니다.
 */
@Service
public class OnboardingAnalysisServiceImpl extends AbstractOpenAiAnalysisService {

	private static final String PROMPT_PATH = "prompts/onboarding-analysis.txt";
	private static final String SCHEMA_NAME = "OnboardingExtraction";

	public OnboardingAnalysisServiceImpl(OpenAiClient openAiClient) {
		super(openAiClient);
	}

	// 온보딩 분석 호출
	public OpenAiResponse analyze(OnboardingAnalysisScheme onboardingInput) {
		return analyzeInternal(onboardingInput);
	}

	// 텍스트 결과만 반환
	public String analyzeText(OnboardingAnalysisScheme onboardingInput) {
		return analyzeTextInternal(onboardingInput);
	}

	@Override
	protected String getPromptPath() {
		return PROMPT_PATH;
	}

	@Override
	protected String getSchemaName() {
		return SCHEMA_NAME;
	}

	@Override
	protected Class<?> getSchemaClass() {
		return OnboardingAnalysisScheme.class;
	}

	@Override
	protected String getInputKey() {
		return "onboarding";
	}
}
