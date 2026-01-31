package com.nect.client.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.client.openai.config.OpenAiProperties;
import com.nect.client.openai.dto.OpenAiErrorResponse;
import com.nect.client.openai.dto.OpenAiResponse;
import com.nect.client.openai.dto.OpenAiResponseRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class OpenAiRestClient implements OpenAiClient {

	private static final String RESPONSES_PATH = "/v1/responses";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final OpenAiProperties properties;

	@Override
	public OpenAiResponse createResponse(OpenAiResponseRequest request) {

		OpenAiResponseRequest normalized = request.withModelDefaults(properties.getModel(), properties.getFallbackModel());
		int maxRetries = Math.max(0, properties.getMaxRetries());
		long backoffMillis = Math.max(0, properties.getInitialBackoffMillis());

		RestClientResponseException lastException = null;
		for (int attempt = 0; attempt <= maxRetries; attempt++) {
			try {
				OpenAiResponse response = restClient.post()
						.uri(RESPONSES_PATH)
						.body(normalized)
						.retrieve()
						.body(OpenAiResponse.class);

				logSuccess(normalized, response, true);
				return response;
			} catch (RestClientResponseException ex) {
				lastException = ex;
				if (!isRetryable(ex.getStatusCode()) || attempt == maxRetries) {
					logFailure(normalized, ex, false);
					throw buildException(ex);
				}

				logFailure(normalized, ex, true);
				sleep(backoffMillis);
				backoffMillis = backoffMillis == 0 ? 0 : backoffMillis * 4;
			}
		}

		throw buildException(lastException);
	}

	private boolean isRetryable(HttpStatusCode statusCode) {
		return statusCode != null && (statusCode.value() == 429
				|| statusCode.value() == 503
				|| statusCode.value() == 504);
	}

	private void sleep(long millis) {
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	private OpenAiClientException buildException(RestClientResponseException ex) {
		if (ex == null) {
			return new OpenAiClientException("OpenAI 호출에 실패했습니다.", null, null, null);
		}
		OpenAiErrorResponse errorResponse = tryParseError(ex.getResponseBodyAsString());
		String message = errorResponse != null && errorResponse.getError() != null ? errorResponse.getError().getMessage() : ex.getStatusText();
		String code = errorResponse != null && errorResponse.getError() != null ? errorResponse.getError().getCode() : null;
		return new OpenAiClientException(message, ex.getStatusCode(), code, ex);
	}

	private OpenAiErrorResponse tryParseError(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(responseBody, OpenAiErrorResponse.class);
		} catch (JsonProcessingException ignored) {
			return null;
		}
	}

	private void logSuccess(OpenAiResponseRequest request, OpenAiResponse response, boolean parsed) {

		String requestId = response != null ? response.getRequestId() : null;
		String model = response != null ? response.getModel() : request.getModel();
		Long totalTokens = response != null && response.getUsage() != null ? response.getUsage().getTotalTokens() : null;

		log.info("openai.responses success requestId={} model={} promptVersion={} inputHash={} totalTokens={} parsed={}",
				requestId,
				model,
				extractPromptVersion(request.getMetadata()),
				hashInput(request.getInput()),
				totalTokens,
				parsed);
	}

	private void logFailure(OpenAiResponseRequest request, RestClientResponseException ex, boolean willRetry) {
		String code = null;
		OpenAiErrorResponse errorResponse = tryParseError(ex.getResponseBodyAsString());
		if (errorResponse != null && errorResponse.getError() != null) {
			code = errorResponse.getError().getCode();
		}

		log.warn("openai.responses failed status={} errorCode={} willRetry={} promptVersion={} inputHash={}",
				ex.getStatusCode().value(),
				code,
				willRetry,
				extractPromptVersion(request.getMetadata()),
				hashInput(request.getInput()));
	}

	private String extractPromptVersion(Map<String, Object> metadata) {
		if (metadata == null) {
			return null;
		}
		Object value = metadata.get("promptVersion");
		if (value == null) {
			value = metadata.get("prompt_version");
		}
		return value != null ? String.valueOf(value) : null;
	}

	private String hashInput(Object input) {
		if (input == null) {
			return null;
		}
		String text = input instanceof String ? (String) input : input.toString();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
