package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class TermsNotAgreedException extends CustomException {
    public TermsNotAgreedException() {
        super(UserErrorCode.TERMS_NOT_AGREED);
    }

    public TermsNotAgreedException(String message) {
        super(UserErrorCode.TERMS_NOT_AGREED, message);
    }
}
