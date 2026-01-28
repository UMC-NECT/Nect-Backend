package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidCredentialsException extends CustomException {
    public InvalidCredentialsException() {
        super(UserErrorCode.INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException(String message) {
        super(UserErrorCode.INVALID_CREDENTIALS, message);
    }
}
