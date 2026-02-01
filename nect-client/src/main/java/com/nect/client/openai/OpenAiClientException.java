package com.nect.client.openai;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class OpenAiClientException extends RuntimeException {

	private final HttpStatusCode statusCode;
	private final String errorCode;

	public OpenAiClientException(String message, HttpStatusCode statusCode, String errorCode, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
		this.errorCode = errorCode;
	}

}
