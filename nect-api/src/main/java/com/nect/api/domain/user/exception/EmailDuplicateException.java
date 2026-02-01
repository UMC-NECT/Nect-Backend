package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class EmailDuplicateException extends CustomException {
    public EmailDuplicateException() {
        super(UserErrorCode.EMAIL_DUPLICATE);
    }

    public EmailDuplicateException(String message) {
        super(UserErrorCode.EMAIL_DUPLICATE, message);
    }
}
