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
public class OpenAiResponseFormat {

	private String type;
	private String name;
	private Map<String, Object> schema;
	private Boolean strict;

	public OpenAiResponseFormat(String type, String name, Map<String, Object> schema, Boolean strict) {
		this.type = type;
		this.name = name;
		this.schema = schema;
		this.strict = strict;
	}

	public static OpenAiResponseFormat jsonSchema(String name, Map<String, Object> schema, Boolean strict) {
		return new OpenAiResponseFormat("json_schema", name, schema, strict);
	}

}
