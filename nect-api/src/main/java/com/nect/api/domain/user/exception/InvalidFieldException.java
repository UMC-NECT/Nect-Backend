package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidFieldException extends CustomException {
    public InvalidFieldException() {
        super(UserErrorCode.INVALID_FIELD);
    }

    public InvalidFieldException(String message) {
        super(UserErrorCode.INVALID_FIELD, message);
    }
}
