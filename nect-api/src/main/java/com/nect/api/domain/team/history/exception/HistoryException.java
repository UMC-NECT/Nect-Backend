package com.nect.api.domain.team.history.exception;

import com.nect.api.domain.team.history.enums.HistoryErrorCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;

@Getter
public class HistoryException extends CustomException {

    public HistoryException(HistoryErrorCode errorCode) {
        super(errorCode);
    }

    public HistoryException(HistoryErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
