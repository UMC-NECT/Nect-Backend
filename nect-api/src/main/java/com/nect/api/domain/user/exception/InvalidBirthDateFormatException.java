package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidBirthDateFormatException extends CustomException {
    public InvalidBirthDateFormatException() {
        super(UserErrorCode.INVALID_BIRTHDATE_FORMAT);
    }

    public InvalidBirthDateFormatException(String message) {
        super(UserErrorCode.INVALID_BIRTHDATE_FORMAT, message);
    }
}
