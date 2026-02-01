package com.nect.api.global.exception;

import com.nect.api.global.code.ResponseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ResponseCode responseCode;

    public CustomException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public CustomException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public CustomException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
    }
}