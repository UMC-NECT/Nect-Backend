package com.nect.api.domain.team.process.exception;

import com.nect.api.domain.team.process.enums.AttachmentErrorCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;

@Getter
public class AttachmentException extends CustomException {

    private final AttachmentErrorCode errorCode;

    public AttachmentException(AttachmentErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public AttachmentException(AttachmentErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }
}