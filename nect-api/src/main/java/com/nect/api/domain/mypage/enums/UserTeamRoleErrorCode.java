package com.nect.api.domain.mypage.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTeamRoleErrorCode implements ResponseCode {

    INVALID_REQUEST("UTR4000", "요청 값이 올바르지 않습니다"),
    INVALID_REQUIRED_COUNT("UTR4001", "모집 인원은 1 이상이어야 합니다"),
    INVALID_CUSTOM_ROLE_NAME("UTR4002", "커스텀 파트명은 필수입니다"),

    FORBIDDEN_NOT_LEADER("UTR4030", "프로젝트 리더만 파트 설정이 가능합니다"),
    FORBIDDEN_NOT_PROJECT_MEMBER("UTR4031", "프로젝트 멤버만 접근할 수 있습니다"),

    USER_NOT_FOUND("UTR4040", "존재하지 않는 사용자입니다"),
    PROJECT_NOT_FOUND("UTR4041", "존재하지 않는 프로젝트입니다"),
    ROLE_NOT_FOUND("UTR4042", "해당 파트를 찾을 수 없습니다"),

    DUPLICATE_ROLE("UTR4090", "이미 존재하는 파트입니다");


    private final String statusCode;
    private final String message;
}
