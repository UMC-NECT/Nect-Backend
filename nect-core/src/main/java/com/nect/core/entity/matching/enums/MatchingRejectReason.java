package com.nect.core.entity.matching.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MatchingRejectReason {
    POSITION_MISMATCH("현재 모집 포지션과 맞지 않음"),
    PROJECT_DURATION_MISMATCH("프로젝트 기간이 맞지 않음"),
    OTHER("기타"),
    ;

    private final String description;
}
