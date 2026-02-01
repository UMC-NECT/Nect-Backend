package com.nect.api.domain.team.workspace.exception;

import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.global.exception.CustomException;

public class BoardsException extends CustomException {
    private final BoardsErrorCode errorCode;

    public BoardsException(BoardsErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public BoardsException(BoardsErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }
}
