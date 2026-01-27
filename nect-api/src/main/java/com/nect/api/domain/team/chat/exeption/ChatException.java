package com.nect.api.domain.team.chat.exeption;

import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;

@Getter
public class ChatException extends CustomException {

    private final ChatErrorCode errorCode;
    
    // 기본 에러
    public ChatException(ChatErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
    // 커스텀 메시지 추가
    public ChatException(ChatErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }

    //커스텀 메시지 + 원인예외
    public ChatException(ChatErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message + " - " + cause.getMessage());
        this.errorCode = errorCode;
    }


}