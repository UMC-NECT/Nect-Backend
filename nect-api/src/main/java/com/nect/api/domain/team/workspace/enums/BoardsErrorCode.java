package com.nect.api.domain.team.workspace.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BoardsErrorCode implements ResponseCode {
    INVALID_REQUEST("B4000", "요청 값이 올바르지 않습니다."),

    PROJECT_MEMBER_FORBIDDEN("B4031", "프로젝트 멤버만 접근할 수 있습니다."),
    PROJECT_LEADER_FORBIDDEN("B4032", "프로젝트 리더만 수정할 수 있습니다."),

    PROJECT_NOT_FOUND("B4041", "프로젝트를 찾을 수 없습니다.");

    private final String statusCode;
    private final String message;
}