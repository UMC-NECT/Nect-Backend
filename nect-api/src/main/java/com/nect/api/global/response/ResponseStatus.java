package com.nect.api.global.response;

import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseStatus {
    private String statusCode;
    private String message;
    private String description;

    public static ResponseStatus ok() {
        return ResponseStatus.builder()
                .statusCode(CommonResponseCode.OK.getStatusCode())
                .message(CommonResponseCode.OK.getMessage())
                .build();
    }

    public static ResponseStatus error() {
        return error(CommonResponseCode.INTERNAL_SERVER_ERROR);
    }

    public static ResponseStatus error(ResponseCode responseCode) {
        return ResponseStatus.builder()
                .statusCode(responseCode.getStatusCode())
                .message(responseCode.getMessage())
                .build();
    }

    public static ResponseStatus error(ResponseCode responseCode, String description) {
        return ResponseStatus.builder()
                .statusCode(responseCode.getStatusCode())
                .message(responseCode.getMessage())
                .description(description)
                .build();
    }
} 