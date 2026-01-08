package com.nect.core.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonResponseCode implements ResponseCode {

    OK("C000", "success"),
    BAD_REQUEST_ERROR("C001", "api bad request exception"),
    REQUEST_BODY_MISSING_ERROR("C002", "required request body is missing"),
    MISSING_REQUEST_PARAMETER_ERROR("C003", "missing servlet requestParameter exception"),
    FORBIDDEN_ERROR("C004", "forbidden exception"),
    NULL_POINT_ERROR("C005", "null point exception"),
    NOT_FOUND_ERROR("C006", "not found exception"),
    NOT_VALID_ERROR("C007", "handle validation exception"),
    NOT_VALID_HEADER_ERROR("C008", "not valid header exception"),
    EXTERNAL_API_ERROR("C009", "external api request failed"),
    INTERNAL_SERVER_ERROR("C999", "internal server error exception")
    ;

    private final String statusCode;
    private final String message;
}