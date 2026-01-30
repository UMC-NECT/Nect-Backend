package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidPasswordException extends CustomException {
    public InvalidPasswordException() {
        super(UserErrorCode.INVALID_PASSWORD);
    }

    public InvalidPasswordException(String message) {
        super(UserErrorCode.INVALID_PASSWORD, message);
    }
}
