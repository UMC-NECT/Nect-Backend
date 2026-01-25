package com.nect.api.domain.team.process.exception;

import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;

@Getter
public class ProcessException extends CustomException {
    private final ProcessErrorCode errorCode;

    public ProcessException(ProcessErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public ProcessException(ProcessErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }
}
