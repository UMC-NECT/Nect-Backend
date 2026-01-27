package com.nect.api.notifications.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;
import com.nect.api.notifications.enums.code.NotificationErrorCode;
import lombok.Getter;

@Getter
public class NotificationException extends CustomException {

    private final ResponseCode errorCode;

    // 기본 에러
    public NotificationException(ResponseCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
    // 커스텀 메시지 추가
    public NotificationException(ResponseCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }

    //커스텀 메시지 + 원인예외
    public NotificationException(ResponseCode errorCode, String message, Throwable cause) {
        super(errorCode, message + " - " + cause.getMessage());
        this.errorCode = errorCode;
    }

}
