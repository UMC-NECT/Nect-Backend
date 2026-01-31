package com.nect.api.domain.team.workspace.exception;

import com.nect.api.domain.team.workspace.enums.PostErrorCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;

@Getter
public class PostException extends CustomException {

    private final PostErrorCode errorCode;

    public PostException(PostErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public PostException(PostErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }
}
