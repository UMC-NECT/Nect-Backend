package com.nect.api.domain.home.enums.code;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HomeErrorCode implements ResponseCode {

    INVALID_HOME_COUNT("H400_1", "count는 1 이상이어야 합니다."),
    INVALID_PARAMETERS("H400_2", "올바르지 않은 파라미터입니다.")

    ;

    private final String statusCode;
    private final String message;

}
