package com.nect.api.domain.team.file.exception;

import com.nect.api.domain.team.file.enums.FileErrorCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;

@Getter
public class FileException extends CustomException {

    private final FileErrorCode errorCode;

    public FileException(FileErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public FileException(FileErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }

    public FileException(FileErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.errorCode = errorCode;
    }
}