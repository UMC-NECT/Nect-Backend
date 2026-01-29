package com.nect.api.domain.matching.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements ResponseCode {

    RECRUITMENT_NOT_OPEN("R400_1", "프로젝트의 해당 분야는 모집중이 아닙니다."),
    ;


    private final String statusCode;
    private final String message;
}

