package com.nect.api.domain.user.enums;

import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ResponseCode {
    EMAIL_DUPLICATE("U001", "이미 사용 중인 이메일입니다"),
    INVALID_PASSWORD("U002", "비밀번호가 일치하지 않습니다"),
    USER_NOT_FOUND("U003", "존재하지 않는 사용자입니다"),
    INVALID_CREDENTIALS("U004", "이메일 또는 비밀번호가 올바르지 않습니다"),
    PASSWORD_MISMATCH("U005", "비밀번호 확인이 일치하지 않습니다"),
    TERMS_NOT_AGREED("U006", "필수 약관에 동의하지 않았습니다"),
    INVALID_REQUEST_FORMAT("U007", "요청 형식이 올바르지 않습니다"),
    INVALID_JOB_TYPE("U008", "직업 타입이 올바르지 않습니다"),
    INVALID_CHECK_TYPE("U009", "검사 타입이 올바르지 않습니다"),
    NICKNAME_DUPLICATE("U010", "이미 사용 중인 닉네임입니다"),
    INVALID_BIRTHDATE_FORMAT("U011", "생년월일 형식이 올바르지 않습니다"),
    INVALID_ROLE("U012", "역할 타입이 올바르지 않습니다"),
    INVALID_FIELD("U013", "직종 타입이 올바르지 않습니다"),
    INVALID_ROLE_FIELD_COMBINATION("U015", "선택한 역할과 맞지 않는 직종입니다"),
    INVALID_GOAL("U014", "목표 타입이 올바르지 않습니다"),
    INVALID_INTEREST_FIELD("U016", "관심분야 타입이 올바르지 않습니다"),
    INVALID_SKILL_CATEGORY("U017", "스킬 카테고리가 올바르지 않습니다"),
    INVALID_COLLABORATION_SCORE("U018", "협업 스타일 점수가 올바르지 않습니다");

    private final String statusCode;
    private final String message;
}
