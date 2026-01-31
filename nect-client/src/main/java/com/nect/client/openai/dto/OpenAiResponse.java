package com.nect.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpenAiResponse {

	private String id;
	private String model;
	@JsonAlias("request_id")
	private String requestId;
	private List<OpenAiOutputItem> output;
	private OpenAiUsage usage;

	@JsonIgnore
	public String getFirstOutputText() {
		if (output == null || output.isEmpty()) {
			return null;
		}
		for (OpenAiOutputItem item : output) {
			String text = item.getFirstText();
			if (text != null) {
				return text;
			}
		}
		return null;
	}
}
