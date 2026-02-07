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
    ROOM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "CHAT_004", "채팅방 이름을 입력해야 합니다"),
    ROOM_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "CHAT_005", "채팅방 이름은 30자를 초과할 수 없습니다"),

    // 멤버 관련
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_101", "채팅방 멤버를 찾을 수 없습니다"),
    CHAT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHAT_102", "이미 채팅방에 참여 중입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_103", "사용자를 찾을 수 없습니다"),
    NO_MEMBERS_SELECTED(HttpStatus.BAD_REQUEST, "CHAT_104", "최소 1명 이상 선택해야 합니다"),

    // 메시지 관련
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_201", "메시지를 찾을 수 없습니다"),
    CHAT_MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_202", "메시지 전송에 실패했습니다"),

    INVALID_MESSAGE_FOR_ROOM(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_203", "해당 방의 메시지가 아닙니다."),
    // Redis 관련
    REDIS_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_301", "Redis 메시지 발행에 실패했습니다"),
    REDIS_SUBSCRIBE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_302", "Redis 메시지 수신에 실패했습니다"),

    // 파일 관련
    CHAT_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_401", "파일을 찾을 수 없습니다"),
    CHAT_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_402", "파일 업로드에 실패했습니다"),

    // 프로젝트 관련
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_501", "프로젝트를 찾을 수 없습니다");




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
