package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidNicknameFormatException extends CustomException {
    public InvalidNicknameFormatException() {
        super(UserErrorCode.INVALID_NICKNAME_FORMAT);
    }

    public InvalidNicknameFormatException(String message) {
        super(UserErrorCode.INVALID_NICKNAME_FORMAT, message);
    }
}