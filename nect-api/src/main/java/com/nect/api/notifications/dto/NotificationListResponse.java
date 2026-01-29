package com.nect.api.notifications.dto;

import com.nect.core.entity.notifications.Notification;
import lombok.Builder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * API 호출 응답에 사용하는 Dto
 * 알림 목록( List<Notification> )을 파라미터에 넣어 호출하면 NotificationResponse의 목록 형태로 변환하여 반환합니다.
 * NotificationResponse은 실시간 알림 전송과 API 호출 응답에 공통적으로 사용됩니다.
 *
 * @param notifications 반환할 알림 목록
 * @param nextCursor 다음 커서 정보
 *
 */
@Builder
public record NotificationListResponse(
        List<NotificationResponse> notifications,
        Long nextCursor
) {

    private static final int LV1_RANGE = 7; // 1주
    private static final int LV2_RANGE = 14; // 2주

    public static NotificationListResponse from(List<Notification> notifications, Long nextCursor) {

        List<NotificationResponse> list = notifications.stream()
                .map(NotificationResponse::from)
                .toList();

        return NotificationListResponse.builder()
                .notifications(list)
                .nextCursor(nextCursor)
                .build();
    }
}
