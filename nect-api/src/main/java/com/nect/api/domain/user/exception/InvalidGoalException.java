package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidGoalException extends CustomException {
    public InvalidGoalException() {
        super(UserErrorCode.INVALID_GOAL);
    }

    public InvalidGoalException(String message) {
        super(UserErrorCode.INVALID_GOAL, message);
    }
}
