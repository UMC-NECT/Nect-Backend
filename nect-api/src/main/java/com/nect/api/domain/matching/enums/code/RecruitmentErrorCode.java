package com.nect.api.domain.matching.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements ResponseCode {

    RECRUITMENT_NOT_OPEN("R400_1", "프로젝트의 해당 분야는 모집중이 아닙니다."),
    FIELD_RECRUITMENT_CLOSED("R400_2", "이미 해당 분야의 모집 인원이 모두 충원되었습니다."),
    PROJECT_RECRUITMENT_NOT_OPEN("R400_3", "해당 프로젝트는 모집 중이 아닙니다."),

    ONLY_LEADER_ACCESS("R403_1", "리더만 접근할 수 있는 기능입니다."),

    NOT_FOUND_RECRUITMENT("R404_1", "해당 모집은 존재하지 않습니다."),
    ;


    private final String statusCode;
    private final String message;
}
