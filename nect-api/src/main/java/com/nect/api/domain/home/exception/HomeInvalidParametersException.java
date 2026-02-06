package com.nect.api.domain.home.exception;

import com.nect.api.domain.home.enums.code.HomeErrorCode;
import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class HomeInvalidParametersException extends CustomException {

    public HomeInvalidParametersException(String message) {
        super(HomeErrorCode.INVALID_PARAMETERS, message);
    }

}
