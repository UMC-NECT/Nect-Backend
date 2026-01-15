package com.nect.api.matching.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingErrorCode implements ResponseCode {

    MATCHING_APPLY_COUNT_EXCEEDED("M400_1", "이미 매칭 대기 중인 프로젝트가 있습니다."),
    MATCHING_INVITE_COUNT_EXCEEDED("M400_2", "해당 프로젝트의 매칭 신청 가능 수를 초과하였습니다."),
    ;

    private final String statusCode;
    private final String message;
}