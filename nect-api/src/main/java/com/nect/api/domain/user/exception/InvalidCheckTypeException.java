package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidCheckTypeException extends CustomException {

    public InvalidCheckTypeException(String message) {
        super(UserErrorCode.INVALID_CHECK_TYPE, message);
    }
}