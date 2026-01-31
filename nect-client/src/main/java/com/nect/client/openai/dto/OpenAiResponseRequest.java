package com.nect.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpenAiResponseRequest {

	private String model;
	private Object input;
	private OpenAiResponseText text;
	private Map<String, Object> metadata;
	private Double temperature;
	@JsonProperty("max_output_tokens")
	private Integer maxOutputTokens;
	@JsonIgnore
	private String fallbackModel;

	public OpenAiResponseRequest(String model, Object input) {
		this.model = model;
		this.input = input;
	}

	@Builder
	public OpenAiResponseRequest(String model,
								 Object input,
								 OpenAiResponseText text,
								 Map<String, Object> metadata,
								 Double temperature,
								 Integer maxOutputTokens,
								 String fallbackModel) {
		this.model = model;
		this.input = input;
		this.text = text;
		this.metadata = metadata;
		this.temperature = temperature;
		this.maxOutputTokens = maxOutputTokens;
		this.fallbackModel = fallbackModel;
	}

	public OpenAiResponseRequest withModelDefaults(String defaultModel, String defaultFallback) {
		if (this.model == null) {
			this.model = defaultModel;
		}
		if (this.fallbackModel == null) {
			this.fallbackModel = defaultFallback;
		}
		return this;
	}

}
