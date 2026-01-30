package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidRoleFieldCombinationException extends CustomException {
    public InvalidRoleFieldCombinationException() {
        super(UserErrorCode.INVALID_ROLE_FIELD_COMBINATION);
    }

    public InvalidRoleFieldCombinationException(String message) {
        super(UserErrorCode.INVALID_ROLE_FIELD_COMBINATION, message);
    }
}