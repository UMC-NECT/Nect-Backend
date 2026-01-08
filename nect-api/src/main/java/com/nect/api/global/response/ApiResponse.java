package com.nect.api.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private ResponseStatus status;
    private T body;

    public static <T> ApiResponse<T> ok() {
        return ApiResponse.ok(null);
    }

    public static <T> ApiResponse<T> ok(T body) {
        var apiResponse = new ApiResponse<T>();
        apiResponse.status = ResponseStatus.ok();
        apiResponse.body = body;
        return apiResponse;
    }

    public static <T> ApiResponse<T> error() {
        var apiResponse = new ApiResponse<T>();
        apiResponse.status = ResponseStatus.error();
        return apiResponse;
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        var apiResponse = new ApiResponse<T>();
        apiResponse.status = ResponseStatus.error(responseCode);
        return apiResponse;
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode, String description) {
        var apiResponse = new ApiResponse<T>();
        apiResponse.status = ResponseStatus.error(responseCode, description);
        return apiResponse;
    }
} 