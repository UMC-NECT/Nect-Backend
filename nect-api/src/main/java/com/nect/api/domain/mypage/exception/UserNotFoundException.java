package com.nect.api.domain.mypage.exception;

import com.nect.api.domain.mypage.enums.MypageErrorCode;
import com.nect.api.global.exception.CustomException;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException() {
        super(MypageErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(MypageErrorCode.USER_NOT_FOUND, message);
    }
}