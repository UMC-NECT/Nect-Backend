package com.nect.api.domain.team.process.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProcessErrorCode implements ResponseCode  {
    INVALID_REQUEST("P4000", "요청 값이 올바르지 않습니다."),
    INVALID_PROCESS_PERIOD("P4001", "프로세스 기간이 올바르지 않습니다."),
    INVALID_PROCESS_STATUS("P4002", "프로세스 상태값이 올바르지 않습니다."),
    INVALID_FEEDBACK_CONTENT("P4003", "피드백 내용이 올바르지 않습니다."),
    INVALID_TASK_ITEM_CONTENT("P4004", "업무 항목 내용이 올바르지 않습니다."),
    INVALID_PROCESS_TITLE_LENGTH("P4005", "프로세스 제목은 최대 50자까지 입력 가능합니다."),
    TASK_ITEM_LIMIT_EXCEEDED("P4006", "업무 항목은 최대 30개까지 등록 가능합니다."),

    FORBIDDEN("P4030", "해당 프로젝트에 대한 권한이 없습니다."),
    PROCESS_NOT_IN_PROJECT("P4031", "해당 프로젝트에 속한 프로세스가 아닙니다."),
    WEEK_MISSION_FORBIDDEN("P4032", "위크 미션은 프로세스 상세 조회에서 조회할 수 없습니다."),

    PROJECT_NOT_FOUND("P4041", "프로젝트를 찾을 수 없습니다."),
    PROCESS_NOT_FOUND("P4042", "프로세스를 찾을 수 없습니다."),
    SHARED_DOCUMENT_NOT_FOUND("P4043", "첨부 문서를 찾을 수 없습니다."),
    TASK_ITEM_NOT_FOUND("P4044", "업무 항목을 찾을 수 없습니다."),
    FEEDBACK_NOT_FOUND("P4045", "피드백을 찾을 수 없습니다."),
    USER_NOT_FOUND("P4046", "사용자를 찾을 수 없습니다.");

    private final String statusCode;
    private final String message;
}
