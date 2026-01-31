package com.nect.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpenAiOutputItem {

	private String type;
	private List<OpenAiOutputContent> content;

	public String getFirstText() {
		if (content == null || content.isEmpty()) {
			return null;
		}
		for (OpenAiOutputContent part : content) {
			String text = part.getText();
			if (text != null && !text.isBlank()) {
				return text;
			}
		}
		return null;
	}
}
