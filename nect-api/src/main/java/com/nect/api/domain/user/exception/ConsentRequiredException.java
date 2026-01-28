package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class ConsentRequiredException extends CustomException {

    public ConsentRequiredException(String message) {
        super(UserErrorCode.TERMS_NOT_AGREED, message);
    }
}