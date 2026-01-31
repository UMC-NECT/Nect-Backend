package com.nect.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpenAiJsonSchema {

	private String name;
	private Map<String, Object> schema;
	private Boolean strict;

	public OpenAiJsonSchema(String name, Map<String, Object> schema, Boolean strict) {
		this.name = name;
		this.schema = schema;
		this.strict = strict;
	}
}
