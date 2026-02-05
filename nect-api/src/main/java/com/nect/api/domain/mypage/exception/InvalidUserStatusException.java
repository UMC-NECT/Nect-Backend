package com.nect.api.domain.mypage.exception;

import com.nect.api.domain.mypage.enums.MypageErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidUserStatusException extends CustomException {
    public InvalidUserStatusException() {
        super(MypageErrorCode.INVALID_USER_STATUS);
    }

    public InvalidUserStatusException(String message) {
        super(MypageErrorCode.INVALID_USER_STATUS, message);
    }
}