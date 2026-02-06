package com.nect.api.domain.notifications.controller;

import com.nect.api.domain.notifications.enums.code.NotificationSearchFilter;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.notifications.dto.NotificationListResponse;
import com.nect.api.domain.notifications.service.NotificationDispatchService;
import com.nect.api.domain.notifications.service.NotificationService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 *
 * 알림 관련 컨트롤러입니다.
 * SSE(Server-Sent Events) 기반를 알림 구독하며, 알림 조회 API를 포함합니다.
 *
 * 클라이언트는 이 엔드포인트를 호출하여
 * 실시간 알림 스트림을 구독합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationDispatchService dispatchService;
    private final NotificationService notificationService;

    /**
     * 실시간 알림 구독
     *
     * - SSE 연결을 생성하고 유지합니다.
     * - 연결 성공 시 "connect" 이벤트를 수신합니다.
     *
     * @return SseEmitter
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return dispatchService.subscribe(userDetails.getUserId());
    }

    /**
     * 개인 알림 조회
     * NotificationScope으로 범위를 결정합니다.
     */
    @GetMapping
    public ApiResponse<NotificationListResponse> notifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("filter") NotificationSearchFilter filter,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        NotificationListResponse response = notificationService.getNotifications(userDetails.getUserId(), filter, cursor, size);
        return ApiResponse.ok(response);
    }

}
