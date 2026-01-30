package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidCollaborationScoreException extends CustomException {
    public InvalidCollaborationScoreException() {
        super(UserErrorCode.INVALID_COLLABORATION_SCORE);
    }

    public InvalidCollaborationScoreException(String message) {
        super(UserErrorCode.INVALID_COLLABORATION_SCORE, message);
    }
}