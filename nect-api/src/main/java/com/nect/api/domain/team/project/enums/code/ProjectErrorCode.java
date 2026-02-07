package com.nect.api.domain.team.project.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectErrorCode implements ResponseCode {

    INVALID_REQUEST("P400_0", "요청 값이 올바르지 않습니다."),

    PROJECT_NOT_FOUND("P400_1", "해당 프로젝트가 존재하지 않습니다."),
    PROJECT_USER_NOT_FOUND("P400_2", "해당 프로젝트 유저가 존재하지 않습니다."),
    ANALYSIS_NOT_FOUND("P400_3", "해당 분석서가 존재하지 않습니다."),
    INVALID_ANALYSIS_DATA("P400_4", "분석서 데이터가 유효하지 않습니다."),
    USER_NOT_FOUND("P400_5", "해당 사용자가 존재하지 않습니다."),
    WEEK_MISSION_NOT_FOUND("P400_6", "해당 위크미션을 찾을 수 없습니다."),
    WEEK_MISSION_ALREADY_INITIALIZED("P400_7", "위크미션이 이미 생성되어 있습니다."),
    INVALID_WEEK_MISSION_UPDATE("P400_8", "수정할 수 없는 항목이 포함되어 있습니다."),

    PROJECT_PART_NOT_FOUND("P400_9", "해당 프로젝트 파트(팀 역할)를 찾을 수 없습니다."),
    DUPLICATE_PART("P400_10", "이미 존재하는 파트입니다."),
    INVALID_CUSTOM_PART_NAME("P400_11", "CUSTOM 파트 이름이 올바르지 않습니다."),


    PROJECT_MEMBER_FORBIDDEN("P403_0", "프로젝트 멤버만 접근할 수 있습니다."),
    LEADER_ONLY_ACTION("P403_1", "리더만 할 수 있는 요청입니다."),
    ;

    private final String statusCode;
    private final String message;
}
