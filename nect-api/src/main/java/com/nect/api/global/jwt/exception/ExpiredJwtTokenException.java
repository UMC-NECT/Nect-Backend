package com.nect.api.global.jwt.exception;

import com.nect.api.global.exception.CustomException;
import com.nect.api.global.jwt.enums.JwtErrorCode;

public class ExpiredJwtTokenException extends CustomException {
    public ExpiredJwtTokenException() {
        super(JwtErrorCode.EXPIRED_JWT_TOKEN);
    }
}