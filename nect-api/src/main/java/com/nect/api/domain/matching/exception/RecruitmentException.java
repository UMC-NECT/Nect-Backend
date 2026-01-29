package com.nect.api.domain.matching.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class RecruitmentException extends CustomException {

    public RecruitmentException(ResponseCode code) {
        super(code);
    }
}
