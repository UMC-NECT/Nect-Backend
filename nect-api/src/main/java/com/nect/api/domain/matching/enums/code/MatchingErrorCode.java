package com.nect.api.domain.matching.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingErrorCode implements ResponseCode {

    MATCHING_APPLY_COUNT_EXCEEDED("M400_1", "이미 매칭 대기 중인 프로젝트가 있습니다."),
    MATCHING_INVITE_COUNT_EXCEEDED("M400_2", "해당 프로젝트의 해당 분야의 매칭 신청 가능 수를 초과하였습니다."),
    MATCHING_STATUS_NOT_CANCELABLE("400_3", "해당 매칭은 취소 불가능한 상태입니다."),
    MATCHING_STATUS_NOT_ACCEPTABLE("400_4", "해당 매칭은 수락 불가능한 상태입니다."),
    MATCHING_STATUS_NOT_REJECTABLE("400_5", "해당 매칭은 거절 불가능한 상태입니다"),

    MATCHING_ACCESS_DENIED("M403_1", "해당 요청에 대한 권한이 없습니다."),

    MATCHING_NOT_FOUND("M404_1", "해당 매칭 요청은 존재하지 않습니다."),
    ;

    private final String statusCode;
    private final String message;
}
