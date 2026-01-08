package com.nect.api.global.jwt.exception;

import com.nect.api.global.exception.CustomException;
import com.nect.api.global.jwt.enums.JwtErrorCode;

public class JwtClaimsEmptyException extends CustomException {
    public JwtClaimsEmptyException() {
        super(JwtErrorCode.JWT_CLAIMS_EMPTY);
    }
}