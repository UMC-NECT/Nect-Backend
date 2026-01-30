package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidJobTypeException extends CustomException {
    public InvalidJobTypeException() {
        super(UserErrorCode.INVALID_JOB_TYPE);
    }

    public InvalidJobTypeException(String message) {
        super(UserErrorCode.INVALID_JOB_TYPE, message);
    }
}