package com.nect.api.global.jwt.exception;

import com.nect.api.global.exception.CustomException;
import com.nect.api.global.jwt.enums.JwtErrorCode;

public class InvalidJwtTokenException extends CustomException {
    public InvalidJwtTokenException() {
        super(JwtErrorCode.INVALID_JWT_TOKEN);
    }
}