package com.nect.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpenAiResponseText {

	private OpenAiResponseFormat format;

	public OpenAiResponseText(OpenAiResponseFormat format) {
		this.format = format;
	}

	public static OpenAiResponseText withFormat(OpenAiResponseFormat format) {
		return new OpenAiResponseText(format);
	}
}
