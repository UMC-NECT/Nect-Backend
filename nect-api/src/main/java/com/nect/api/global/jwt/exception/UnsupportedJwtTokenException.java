package com.nect.api.global.jwt.exception;

import com.nect.api.global.exception.CustomException;
import com.nect.api.global.jwt.enums.JwtErrorCode;

public class UnsupportedJwtTokenException extends CustomException {
    public UnsupportedJwtTokenException() {
        super(JwtErrorCode.UNSUPPORTED_JWT_TOKEN);
    }
}