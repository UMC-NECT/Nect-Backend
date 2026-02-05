package com.nect.api.global.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageErrorCode implements ResponseCode {

    EMPTY_FILE("400_1", "파일이 비어있습니다."),
    EMPTY_FILE_NAME("400_2", "파일 이름이 비어있습니다."),
    INVALID_FILE_TYPE("400_3", "이미지 파일(jpg, jpeg, png)만 업로드 가능합니다."),
    FILE_UPLOAD_FAILED("500_1", "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND("404_1", "파일이 존재하지 않습니다."),
    CHAT_ROOM_NOT_FOUND("404_2", "존재하지 않는 채팅방입니다."),
    NOT_CHAT_ROOM_MEMBER("403_1", "해당 채팅방에 참여 중인 사용자가 아닙니다."),
    FILE_DOWNLOAD_FAILED("500_2", "파일 다운로드에 실패했습니다.");

    private final String statusCode;
    private final String message;
}
