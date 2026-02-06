package com.nect.api.global.ai.service;

import com.nect.api.domain.user.dto.ProfileDto;
import com.nect.api.global.ai.dto.OnboardingAnalysisScheme;
import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.dto.OpenAiResponse;
import org.springframework.stereotype.Service;

@Service
public class OnboardingAnalysisServiceImpl extends AbstractOpenAiAnalysisService {

	private static final String PROMPT_PATH = "prompts/onboarding-analysis.txt";
	private static final String SCHEMA_NAME = "OnboardingExtraction";

	public OnboardingAnalysisServiceImpl(OpenAiClient openAiClient) {
		super(openAiClient);
	}

	public OpenAiResponse analyzeProfile(ProfileDto.ProfileSetupRequestDto profileRequest) {
		return analyzeInternal(profileRequest);
	}

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
