package com.nect.api.domain.team.project.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectErrorCode implements ResponseCode {

    PROJECT_NOT_FOUND("P400_1", "해당 프로젝트가 존재하지 않습니다."),
    PROJECT_USER_NOT_FOUND("P400_2", "해당 프로젝트 유저가 존재하지 않습니다."),

    LEADER_ONLY_ACTION("P403_1", "리더만 할 수 있는 요청입니다."),
    ;

    private final String statusCode;
    private final String message;
}
