package com.nect.client.openai;

import com.nect.client.openai.dto.OpenAiResponseRequest;
import com.nect.client.openai.dto.OpenAiResponse;

public interface OpenAiClient {

	OpenAiResponse createResponse(OpenAiResponseRequest request);

}
