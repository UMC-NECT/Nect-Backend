package com.nect.api.domain.notifications.dto;

import com.nect.core.entity.notifications.Notification;
import java.time.format.DateTimeFormatter;

/**
 *
 * 실시간 알림 응답과 알림 API 호출에서 공통으로 사용하는 응답 모델입니다.
 *
 * @param mainMessage 알림에 대한 주 내용입니다.
 * @param contentMessage 알림에 대해 부가적인 내용( 댓글내용, 파일명 등 )입니다.
 * @param noticeId 알림 식별자(pk)
 * @param targetId 알림을 클릭하고 상세 조회 페이지 api를 호출할 때 사용할 식별자입니다.
 *                 프론트에서 type으로 구분하여 적절히 사용합니다.
 * @param createdDate 생성일
 * @param classification 분류명
 * @param type 알림 유형 - 알림 메시지 포맷을 담습니다.
 * @param scope 알림 대상 - 알림 받는 화면입니다.
 *
 */
public record NotificationResponse(

        String mainMessage,
        String contentMessage,
        Long noticeId,
        Long targetId,
        Long projectId,
        String createdDate,
        String classification,
        String type,
        String scope,
        Boolean isRead

) {

    public static NotificationResponse from(Notification notification) {
        Long projectId = (notification.getProject() != null)
                ? notification.getProject().getId()
                : null;

        return new NotificationResponse(
                notification.getMainMessage(),
                notification.getContentMessage(),
                notification.getId(),
                notification.getTargetId(),
                projectId,
                notification.getCreatedAt().format(FORMATTER),
                notification.getClassification().getClassifyKr(),
                notification.getType().name(),
                notification.getScope().name(),
                notification.getIsRead()
        );
    }

    // LocalDateTime -> "yy.MM.dd" 형식 변환 formatter
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yy.MM.dd");
}
