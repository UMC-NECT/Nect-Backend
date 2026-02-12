package com.nect.core.entity.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberMatchable {

    MATCHABLE("매칭 가능"),
    MATCH_COMPLETE("매칭 완료"),

    ;

    private final String description;

}
