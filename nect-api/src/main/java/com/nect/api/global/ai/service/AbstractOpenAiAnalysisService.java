package com.nect.api.global.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.nect.api.global.ai.enums.OpenAiExceptionCode;
import com.nect.api.global.ai.exception.OpenAiException;
import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.dto.OpenAiResponse;
import com.nect.client.openai.dto.OpenAiResponseFormat;
import com.nect.client.openai.dto.OpenAiResponseRequest;
import com.nect.client.openai.dto.OpenAiResponseText;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 분석 요청을 처리하는 공통 서비스입니다.
 *
 * 프롬프트 로딩, 응답 스키마 생성, 요청 전송까지의
 * 기본 흐름을 제공하며, 상세 구성은 하위 클래스에서 정의합니다.
 */
@RequiredArgsConstructor
abstract class AbstractOpenAiAnalysisService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final SchemaGenerator SCHEMA_GENERATOR = createSchemaGenerator();

	private final OpenAiClient openAiClient;

	protected OpenAiResponse analyzeInternal(Object rawInput) {
		try {
			String prompt = loadPrompt(getPromptPath());
			Object input = buildInput(prompt, rawInput);
			OpenAiResponseRequest request = OpenAiResponseRequest.builder()
					.input(input)
					.text(OpenAiResponseText.withFormat(buildResponseFormat()))
					.build();
			return openAiClient.createResponse(request);
		} catch (OpenAiException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new OpenAiException(OpenAiExceptionCode.OPENAI_REQUEST_FAILED, "OpenAI 요청 처리 중 오류가 발생했습니다.", ex);
		}
	}

	protected String analyzeTextInternal(Object rawInput) {
		OpenAiResponse response = analyzeInternal(rawInput);
		return response != null ? response.getFirstOutputText() : null;
	}

	protected abstract String getPromptPath();

	protected abstract String getSchemaName();

	protected abstract Class<?> getSchemaClass();

	protected String getInputKey() {
		return "input";
	}

	protected Object buildInput(String prompt, Object rawInput) {
		String inputJson;
		try {
			inputJson = rawInput == null ? "{}" : OBJECT_MAPPER.writeValueAsString(rawInput);
		} catch (Exception ex) {
			inputJson = String.valueOf(rawInput);
		}

		StringBuilder text = new StringBuilder();
		if (prompt != null && !prompt.isBlank()) {
			text.append(prompt).append("\n\n");
		}
		text.append("Input JSON:\n").append(inputJson);

		return List.of(
				Map.of(
						"role", "user",
						"content", List.of(
								Map.of(
										"type", "input_text",
										"text", text.toString()
								)
						)
				)
		);
	}

	private OpenAiResponseFormat buildResponseFormat() {
		return OpenAiResponseFormat.jsonSchema(
				getSchemaName(),
				buildResponseSchema(),
				true
		);
	}

	private Map<String, Object> buildResponseSchema() {
		try {
			JsonNode schemaNode = SCHEMA_GENERATOR.generateSchema(getSchemaClass());
			Map<String, Object> schema = OBJECT_MAPPER.convertValue(schemaNode, new TypeReference<Map<String, Object>>() {});
			normalizeSchema(schema);
			return schema;
		} catch (Exception ex) {
			throw new OpenAiException(OpenAiExceptionCode.OPENAI_SCHEMA_GENERATION_FAILED, "OpenAI 스키마 생성에 실패했습니다.", ex);
		}
	}

	@SuppressWarnings("unchecked")
	private void normalizeSchema(Object node) {
		if (node instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) node;
			Object type = map.get("type");
			Object properties = map.get("properties");
			if (type instanceof String && "object".equals(type) && properties instanceof Map) {
				Map<String, Object> props = (Map<String, Object>) properties;
				if (!map.containsKey("additionalProperties")) {
					map.put("additionalProperties", false);
				}
				Object required = map.get("required");
				if (!(required instanceof List)) {
					map.put("required", new java.util.ArrayList<>(props.keySet()));
				} else {
					List<Object> requiredList = (List<Object>) required;
					for (String key : props.keySet()) {
						boolean exists = false;
						for (Object item : requiredList) {
							if (key.equals(item)) {
								exists = true;
								break;
							}
						}
						if (!exists) {
							requiredList.add(key);
						}
					}
				}
			}

			for (Object value : map.values()) {
				normalizeSchema(value);
			}
			return;
		}

		if (node instanceof List) {
			List<Object> list = (List<Object>) node;
			for (Object item : list) {
				normalizeSchema(item);
			}
		}
	}

	private static SchemaGenerator createSchemaGenerator() {
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
				OBJECT_MAPPER,
				SchemaVersion.DRAFT_2020_12,
				OptionPreset.PLAIN_JSON
		);
		SchemaGeneratorConfig config = configBuilder.build();
		return new SchemaGenerator(config);
	}

	private String loadPrompt(String path) {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
			if (inputStream == null) {
				throw new OpenAiException(OpenAiExceptionCode.OPENAI_PROMPT_LOAD_FAILED, "프롬프트 파일을 찾을 수 없습니다: " + path);
			}
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new OpenAiException(OpenAiExceptionCode.OPENAI_PROMPT_LOAD_FAILED, "프롬프트 파일을 읽는 중 오류가 발생했습니다.", ex);
		}
	}
}
