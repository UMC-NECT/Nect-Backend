package com.nect.api.domain.team.workspace.exception;


import com.nect.api.domain.team.workspace.enums.ScheduleErrorCode;
import com.nect.api.global.exception.CustomException;

public class ScheduleException extends CustomException {

    private final ScheduleErrorCode errorCode;

    public ScheduleException(ScheduleErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public ScheduleException(ScheduleErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }
}
