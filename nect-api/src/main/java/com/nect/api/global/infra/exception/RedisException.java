package com.nect.api.global.infra.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class RedisException extends CustomException {

    public RedisException(ResponseCode code) {
        super(code);
    }

    public RedisException(ResponseCode code, String message) {
        super(code, message);
    }

    public RedisException(ResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
