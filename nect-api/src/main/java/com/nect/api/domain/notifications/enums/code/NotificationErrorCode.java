package com.nect.api.domain.notifications.enums.code;


import com.nect.api.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ResponseCode {

    // 코드 차원
    NOTIFICATION_SUBSCRIBE_FAILED("N001", "알림 구독에 실패했습니다"),
    SSE_SEND_FAILED("N002", "실시간 알림 전송에 실패했습니다"),
    NOTIFICATION_NOT_FOUND("N003", "알림을 찾을 수 없습니다"),
    INVALID_NOTIFICATION_SCOPE("N004", "유효하지 않은 알림 범위입니다"),
    EMITTER_NOT_FOUND("N005", "알림 연결 정보가 존재하지 않습니다"),

    // 비즈니스 차원
    NOTIFICATION_LENGTH_EXCEED("N006", "알림 내용 길이가 초과했습니다. 관리자에게 문의해주세요"),
    INVALID_NOTIFICATION_MESSAGE_FORMAT("N007", "알림 메시지 형식이 올바르지 않습니다"),
    NOTIFICATION_CONTENT_NOT_SUPPORTED("N008", "해당 알림은 상세 내용을 지원하지 않습니다");
    ;

    private final String statusCode;
    private final String message;

}
