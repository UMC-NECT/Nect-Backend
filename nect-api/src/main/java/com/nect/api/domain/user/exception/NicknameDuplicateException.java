package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class NicknameDuplicateException extends CustomException {
    public NicknameDuplicateException() {
        super(UserErrorCode.NICKNAME_DUPLICATE);
    }

    public NicknameDuplicateException(String message) {
        super(UserErrorCode.NICKNAME_DUPLICATE, message);
    }
}
