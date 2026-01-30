package com.nect.api.domain.team.process.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttachmentErrorCode implements ResponseCode {
    INVALID_REQUEST("A4001", "요청 값이 올바르지 않습니다."),

    PROJECT_NOT_FOUND("A4041", "프로젝트를 찾을 수 없습니다."),
    PROCESS_NOT_FOUND("A4042", "프로세스를 찾을 수 없습니다."),
    FILE_NOT_FOUND("A4043", "파일(문서)을 찾을 수 없습니다."),
    LINK_NOT_FOUND("A4044", "링크를 찾을 수 없습니다."),

    FILE_ALREADY_ATTACHED("A4091", "이미 첨부된 파일입니다."),
    FILE_NOT_ATTACHED("A4045", "첨부되지 않은 파일입니다.");

    private final String statusCode;
    private final String message;
}