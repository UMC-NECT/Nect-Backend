package com.nect.core.entity.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecruitmentStatus {

    UPCOMING("모집 예정"),
    OPEN("모집 중"),
    CLOSED("모집 종료")
    ;

    private final String status;

}
