package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class PasswordMismatchException extends CustomException {
    public PasswordMismatchException() {
        super(UserErrorCode.PASSWORD_MISMATCH);
    }

    public PasswordMismatchException(String message) {
        super(UserErrorCode.PASSWORD_MISMATCH, message);
    }
}
