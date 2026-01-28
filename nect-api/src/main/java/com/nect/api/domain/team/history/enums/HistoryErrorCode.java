package com.nect.api.domain.team.history.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HistoryErrorCode implements ResponseCode {
    INVALID_REQUEST("H4001", "요청 값이 올바르지 않습니다."),

    PROJECT_NOT_FOUND("H4041", "프로젝트를 찾을 수 없습니다."),
    HISTORY_NOT_FOUND("H4042", "히스토리를 찾을 수 없습니다.");

    private final String statusCode;
    private final String message;
}
