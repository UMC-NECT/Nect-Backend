package com.nect.client.openai.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

	private String apiKey;
	private String baseUrl = "https://api.openai.com";
	private String model = "gpt-4o-mini";
	private String fallbackModel = "gpt-4.1";
	private int connectTimeoutSeconds = 2;
	private int readTimeoutSeconds = 20;
	private int maxRetries = 2;
	private long initialBackoffMillis = 200;

}
