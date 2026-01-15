package com.nect.api.matching.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class MatchingException extends CustomException {

    public MatchingException(ResponseCode code) {
        super(code);
    }
}
