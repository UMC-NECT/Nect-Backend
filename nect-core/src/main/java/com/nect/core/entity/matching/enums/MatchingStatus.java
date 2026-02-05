package com.nect.core.entity.matching.enums;

public enum MatchingStatus {
    PENDING, // 진행
    ACCEPTED, // 수락
    REJECTED, // 거절
    EXPIRED, // 만료
    CANCELED // 취소
    ;

    public static MatchingStatus from(String value) {
        return MatchingStatus.valueOf(value.toUpperCase());
    }
}
