package com.nect.api.global.ai.service;

import com.nect.api.global.ai.dto.IdeaAnalysisScheme;
import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.dto.OpenAiResponse;
import org.springframework.stereotype.Service;

/**
 * 아이디어 분석 요청을 OpenAI로 전달하고
 * 결과를 반환하는 서비스입니다.
 */
@Service
public class IdeaAnalysisServiceImpl extends AbstractOpenAiAnalysisService {

	private static final String PROMPT_PATH = "prompts/idea-analysis.txt";
	private static final String SCHEMA_NAME = "IdeaExtraction";

	public IdeaAnalysisServiceImpl(OpenAiClient openAiClient) {
		super(openAiClient);
	}

	// 아이디어 분석 호출
	public OpenAiResponse analyze(IdeaAnalysisScheme ideaInput) {
		return analyzeInternal(ideaInput);
	}

	// 텍스트 결과만 반환
	public String analyzeText(IdeaAnalysisScheme ideaInput) {
		return analyzeTextInternal(ideaInput);
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
		return IdeaAnalysisScheme.class;
	}

	@Override
	protected String getInputKey() {
		return "idea";
	}
}
