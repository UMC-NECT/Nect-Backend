package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(UserErrorCode.USER_NOT_FOUND, message);
    }
}
