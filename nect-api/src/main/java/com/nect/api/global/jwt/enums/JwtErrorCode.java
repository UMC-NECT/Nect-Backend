package com.nect.api.global.jwt.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JwtErrorCode implements ResponseCode {

    EXPIRED_JWT_TOKEN("J001", "Expired JWT token"),
    INVALID_JWT_TOKEN("J002", "Invalid JWT token"),
    JWT_CLAIMS_EMPTY("J003", "JWT Claims is empty"),
    UNSUPPORTED_JWT_TOKEN("J004", "Unsupported JWT token");

    private final String statusCode;
    private final String message;
}