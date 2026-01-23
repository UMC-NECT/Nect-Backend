package com.nect.api.notifications.controller;

import com.nect.api.notifications.facade.NotificationFacade;
import com.nect.api.notifications.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationDispatchService dispatchService;
    private final NotificationFacade notificationFacade;

    /**
     * 실시간 알림 구독
     *
     * - SSE 연결을 생성하고 유지합니다.
     * - 연결 성공 시 "connect" 이벤트를 수신합니다.
     *
     * @return SseEmitter
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {

        Long userId = 1L;

        return dispatchService.subscribe(userId);
    }

}
