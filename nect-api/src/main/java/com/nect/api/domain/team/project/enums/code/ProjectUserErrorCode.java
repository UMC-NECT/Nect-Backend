package com.nect.api.domain.team.project.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProjectUserErrorCode implements ResponseCode {

    PROJECT_USER_NOT_FOUND("PU400_1", "해당 프로젝트 유저를 찾을 수 없습니다."),
    CUSTOM_FIELD_REQUIRED("PU400_2", "커스텀 필드는 필수로 입력해야합니다."),

    ONLY_LEADER_ALLOWED("PU403_1", "프로젝트 리더만 접근할 수 있는 기능입니다."),
    ;

    private final String statusCode;
    private final String message;
}
