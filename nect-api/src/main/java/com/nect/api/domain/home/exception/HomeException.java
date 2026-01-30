package com.nect.api.domain.home.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class HomeException extends CustomException {

    public HomeException(ResponseCode code) {
        super(code);
    }

}
