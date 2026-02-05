package com.nect.api.domain.team.chat.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ResponseCode {

    // 채팅방 관련
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다"),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_002", "채팅방에 접근할 권한이 없습니다"),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHAT_003", "이미 존재하는 채팅방입니다"),

    // 멤버 관련
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_101", "채팅방 멤버를 찾을 수 없습니다"),
    CHAT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHAT_102", "이미 채팅방에 참여 중입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_103", "사용자를 찾을 수 없습니다"),
    // 메시지 관련
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_201", "메시지를 찾을 수 없습니다"),
    CHAT_MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_202", "메시지 전송에 실패했습니다"),

    // 파일 관련
    CHAT_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_401", "파일을 찾을 수 없습니다"),
    CHAT_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_402", "파일 업로드에 실패했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public String getStatusCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
