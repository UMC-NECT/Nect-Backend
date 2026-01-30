package com.nect.api.domain.team.file.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements ResponseCode {
    INVALID_REQUEST("F4001", "요청 값이 올바르지 않습니다."),
    EMPTY_FILE("F4002", "업로드 파일이 비어있습니다."),
    FILE_SIZE_EXCEEDED("F4003", "파일 크기가 제한을 초과했습니다."),
    UNSUPPORTED_FILE_EXT("F4004", "지원하지 않는 파일 확장자입니다."),

    PROJECT_NOT_FOUND("F4041", "프로젝트를 찾을 수 없습니다."),
    FILE_NOT_FOUND("F4042", "파일(문서)을 찾을 수 없습니다.");

    private final String statusCode;
    private final String message;
}