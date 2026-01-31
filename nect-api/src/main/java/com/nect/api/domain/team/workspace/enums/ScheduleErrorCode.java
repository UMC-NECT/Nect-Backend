package com.nect.api.domain.team.workspace.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements ResponseCode {

    INVALID_REQUEST("S4000", "요청 값이 올바르지 않습니다."),

    PROJECT_MEMBER_FORBIDDEN("S4030", "프로젝트 멤버만 접근할 수 있습니다."),
    SCHEDULE_EDITOR_FORBIDDEN("S4031", "일정을 수정/삭제할 권한이 없습니다."),

    PROJECT_NOT_FOUND("S4041", "프로젝트를 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND("S4042", "일정을 찾을 수 없습니다.");

    private final String statusCode;
    private final String message;
}