package com.nect.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpenAiUsage {

	@JsonAlias("input_tokens")
	private Long inputTokens;
	@JsonAlias("output_tokens")
	private Long outputTokens;
	@JsonAlias("total_tokens")
	private Long totalTokens;

}
