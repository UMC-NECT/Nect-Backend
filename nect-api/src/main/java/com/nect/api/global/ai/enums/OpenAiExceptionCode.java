package com.nect.api.global.ai.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OpenAI 처리 과정에서 사용하는 예외 코드를 정의합니다.
 */
@Getter
@AllArgsConstructor
public enum OpenAiExceptionCode implements ResponseCode {

    OPENAI_REQUEST_FAILED("AI001", "OpenAI 요청에 실패했습니다."),
    OPENAI_RESPONSE_PARSE_FAILED("AI002", "OpenAI 응답 파싱에 실패했습니다."),
    OPENAI_PROMPT_LOAD_FAILED("AI003", "OpenAI 프롬프트 로딩에 실패했습니다."),
    OPENAI_SCHEMA_GENERATION_FAILED("AI004", "OpenAI 스키마 생성에 실패했습니다.");

    private final String statusCode;
    private final String message;

}
