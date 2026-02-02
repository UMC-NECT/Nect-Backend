package com.nect.api.global.infra.exception;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageErrorCode implements ResponseCode {

    EMPTY_FILE("400_1", "파일이 비어있습니다."),
    EMPTY_FILE_NAME("400_2", "파일 이름이 비어있습니다.");
    ;

    private final String statusCode;
    private final String message;
}
