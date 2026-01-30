package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidRoleException extends CustomException {
    public InvalidRoleException() {
        super(UserErrorCode.INVALID_ROLE);
    }

    public InvalidRoleException(String message) {
        super(UserErrorCode.INVALID_ROLE, message);
    }
}
