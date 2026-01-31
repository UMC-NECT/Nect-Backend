package com.nect.client.openai.config;

import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.OpenAiRestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiClientConfig {

	@Bean
	public RestClient openAiRestClient(OpenAiProperties properties) {

		if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
			throw new IllegalStateException("OpenAI API 키가 없습니다. OPENAI_API_KEY 또는 openai.api-key를 설정하세요.");
		}

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()));
		requestFactory.setReadTimeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()));

		return RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.requestFactory(requestFactory)
				.defaultHeader("Authorization", "Bearer " + properties.getApiKey())
				.defaultHeader("Content-Type", "application/json")
				.build();
	}

	@Bean
	public OpenAiClient openAiClient(RestClient openAiRestClient, ObjectMapper objectMapper, OpenAiProperties properties) {
		return new OpenAiRestClient(openAiRestClient, objectMapper, properties);
	}
	
}
