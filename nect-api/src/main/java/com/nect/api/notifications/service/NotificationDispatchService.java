package com.nect.api.notifications.service;

import com.nect.api.notifications.dto.NotificationResponse;
import com.nect.core.entity.notifications.Notification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.repository.notifications.EmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

/**
 * SSE(Server-Sent Events)를 이용해
 * 저장된 Notification을 클라이언트로 실시간 전송하는 서비스입니다.
 *
 * 이 서비스는
 * - SSE 구독(Emitter 생성 및 관리)
 * - 특정 유저에게 알림 이벤트 전송
 * 책임만을 가지며,
 *
 * 알림 생성/저장은 NotificationService,
 * 알림 생성 + 전송 흐름 제어는 NotificationFacade에서 담당합니다.
 *
 * 하나의 유저는 여러 개의 SSE 연결(여러 탭, 여러 디바이스)을 가질 수 있으며,
 * 모든 연결에 동일한 알림을 브로드캐스트 방식으로 전송합니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final EmitterRepository emitterRepository;

    private static final long DEFAULT_TIMEOUT = 5L * 60 * 1000; // emitter 객체 수명 15분

    /**
     * SSE 구독 요청을 처리합니다.
     *
     * - 클라이언트가 최초로 알림 구독 시 호출됩니다.
     * - SseEmitter를 생성하고 연결 확인용 이벤트(connect)를 전송합니다.
     * - 유저 ID 기준으로 emitter를 저장하여
     *   다중 탭 / 다중 디바이스 환경을 지원합니다.
     *
     * 연결 종료, 타임아웃, 에러 발생 시
     * 해당 emitter는 자동으로 제거됩니다.
     */
    public SseEmitter subscribe(Long userId) {

        // 현재 클라이언트에 대한 emitter 생성
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 최초 연결 확인용 이벤트 전송 (프론트에서 연결 성공 판단)
        try {
            sseEmitter.send(
                    SseEmitter
                        .event()
                        .name("connect")
            );
        } catch (IOException e) {
            return sseEmitter; // 이미 연결이 끊김 시점에서는 emitter 저장하지 않음
        }

        // userId 기준으로 emitter 저장 (다중 탭 / 다중 디바이스 대응)
        emitterRepository.addEmitter(userId, sseEmitter);

        // emitter 제거하는 경우
        sseEmitter.onCompletion(() -> emitterRepository.remove(userId)); // 정상 종료 시
        sseEmitter.onTimeout(() -> emitterRepository.remove(userId)); // 타임아웃 발생 시
        sseEmitter.onError((e) -> emitterRepository.remove(userId)); // 에러 발생 시

        return sseEmitter;
    }

    /**
     * 특정 유저에게 실시간 알림을 전송합니다.
     *
     * - 해당 유저가 가지고 있는 모든 SSE 연결(emitter)에 대해
     *   동일한 알림 이벤트를 전송합니다.
     * - 전송 중 IOException 발생 시
     *   연결이 끊긴 emitter로 판단하고 즉시 제거합니다.
     *
     * @param notification 전송할 알림 엔티티
     */
    public void send(Notification notification) {

        // user에 대한 모든 emitters 조회
        Long receiverId = notification.getReceiver().getUserId();
        Set<SseEmitter> emitters = emitterRepository.getAll(receiverId);

        // 유저에 대한 emitter 없을 시 return
        if (emitters == null || emitters.isEmpty())
            return;

        NotificationType type = notification.getType();
        NotificationScope scope = notification.getScope();
        String eventId = receiverId + "_" + scope.getEventName() + "_" + System.currentTimeMillis();

        // ConcurrentModificationException 방지를 위해 복사본 사용
        for (SseEmitter emitter : new ArrayList<>(emitters)) {

            int emitterCode = System.identityHashCode(emitter); // 이후 로그 작성을 위해 생성
            try {
                // 실제 SSE 이벤트 전송
                emitter.send(SseEmitter.event()
                        .name(scope.getEventName())
                        .id(eventId)
                        .data(NotificationResponse.from(notification))
                );
            } catch (IOException e) { // 전송 실패 = 연결이 끊긴 emitter로 판단

                emitterRepository.remove(receiverId, emitter);

                try { // emitter 닫기
                    emitter.complete();
                } catch (Exception closeEx) {
                    // TODO: 예외로그
                }
            }
        }
    }

}
