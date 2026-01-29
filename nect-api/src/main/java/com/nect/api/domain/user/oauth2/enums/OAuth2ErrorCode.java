package com.nect.api.domain.user.oauth2.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OAuth2ErrorCode implements ResponseCode {

    OAUTH2_PROVIDER_NOT_FOUND("A001", "OAuth2 provider를 찾을 수 없습니다"),
    UNSUPPORTED_OAUTH2_PROVIDER("A002", "지원하지 않는 OAuth2 provider입니다"),
    OAUTH2_USER_INFO_INVALID("A003", "OAuth2 사용자 정보가 유효하지 않습니다"),
    OAUTH2_AUTHENTICATION_FAILED("A004", "OAuth2 인증이 실패했습니다")
    ;

    private final String statusCode;
    private final String message;
}