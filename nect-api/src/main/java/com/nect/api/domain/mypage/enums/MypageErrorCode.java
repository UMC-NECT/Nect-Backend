package com.nect.api.domain.mypage.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MypageErrorCode implements ResponseCode {
    USER_NOT_FOUND("M001", "존재하지 않는 사용자입니다"),
    INVALID_USER_STATUS("M002", "유효하지 않은 사용자 상태입니다");

    private final String statusCode;
    private final String message;
}