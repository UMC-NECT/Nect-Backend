package com.nect.api.global.exception;

import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Object> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("HTTP Method not allowed: {}", e.getMessage());
        return ApiResponse.error(CommonResponseCode.BAD_REQUEST_ERROR, "Method Not Allowed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        StringBuilder errorMessage = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage()).append(", ");
        }

        String message = !errorMessage.isEmpty()
                ? errorMessage.substring(0, errorMessage.length() - 2)
                : "유효하지 않은 요청입니다";

        return ApiResponse.error(CommonResponseCode.NOT_VALID_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Missing required parameter: {}", e.getMessage());
        String message = "Missing required parameter : " + e.getParameterName();
        return ApiResponse.error(CommonResponseCode.NOT_VALID_ERROR, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Request body missing or malformed: {}", e.getMessage());
        String message = "Required request body is missing or malformed";
        return ApiResponse.error(CommonResponseCode.NOT_VALID_ERROR, message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ApiResponse.error(CommonResponseCode.NOT_FOUND_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMessage());

        // 유니크 제약 조건 위반
        if (e.getCause() instanceof ConstraintViolationException) {
            return ApiResponse.error(CommonResponseCode.BAD_REQUEST_ERROR, "중복된 요청입니다.");
        }

        // 그 외 무결성 제약 위반
        return ApiResponse.error(CommonResponseCode.BAD_REQUEST_ERROR, "데이터 무결성 예외가 발생했습니다.");
    }

    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleCustomException(CustomException e) {
        log.warn("Custom exception occurred: {}", e.getMessage());
        if (!e.getMessage().equals(e.getResponseCode().getMessage())) {
            return ApiResponse.error(e.getResponseCode(), e.getMessage());
        } else {
            return ApiResponse.error(e.getResponseCode());
        }
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleRuntimeException(RuntimeException e) {
        if (e instanceof CustomException) {
            return handleCustomException((CustomException) e);
        }
        log.warn("Unhandled runtime exception occurred: {}", e.getMessage());
        return ApiResponse.error(CommonResponseCode.BAD_REQUEST_ERROR, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleException(Exception e) {
        log.error("Exception occurred: ", e);
        return ApiResponse.error(CommonResponseCode.INTERNAL_SERVER_ERROR);
    }
}