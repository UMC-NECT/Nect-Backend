package com.nect.api.global.infra.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class StorageException extends CustomException {
    public StorageException(ResponseCode code) {
        super(code);
    }

}
