package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidInterestFieldException extends CustomException {
    public InvalidInterestFieldException() {
        super(UserErrorCode.INVALID_INTEREST_FIELD);
    }

    public InvalidInterestFieldException(String message) {
        super(UserErrorCode.INVALID_INTEREST_FIELD, message);
    }
}