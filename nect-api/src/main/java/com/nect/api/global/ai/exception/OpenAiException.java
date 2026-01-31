package com.nect.api.global.ai.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;

/**
 * OpenAI 호출/처리 과정에서 발생하는 예외를
 * 공통 응답 규격에 맞춰 래핑하는 예외입니다.
 */
@Getter
public class OpenAiException extends CustomException {

    private final ResponseCode errorCode;

    public OpenAiException(ResponseCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public OpenAiException(ResponseCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }

    public OpenAiException(ResponseCode errorCode, String message, Throwable cause) {
        super(errorCode, message + " - " + cause.getMessage());
        this.errorCode = errorCode;
    }

}
