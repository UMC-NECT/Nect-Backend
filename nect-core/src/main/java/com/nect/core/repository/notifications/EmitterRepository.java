package com.nect.core.repository.notifications;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE(Server-Sent Events) 기반 실시간 알림 전송을 위해
 * 유저별 SseEmitter 연결을 관리하는 인메모리 저장소입니다.
 *
 * 하나의 유저는 여러 개의 SSE 연결(여러 탭, 여러 디바이스)을
 * 가질 수 있으며, 각 유저 ID를 기준으로 Emitter Set을 관리합니다.
 *
 * 이 클래스는
 * - Emitter 등록 / 제거
 * - 유저별 활성 Emitter 조회
 * 책임만을 가지며,
 * 실제 알림 전송 로직은 NotificationDispatchService에서 처리됩니다.
 *
 * ⚠️ 서버 재시작 시 모든 연결 정보는 소멸됩니다.
 */

@Repository
public class EmitterRepository {

    // 유저ID -> 연결된 모든 SSE Emitter
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // emitter 추가
    public void addEmitter(Long userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(emitter);
    }

    // 특정 유저의 emitter 모두 제거
    public void remove(Long userId) {

        // 특정 유저 emitter 존재 여부
        boolean exists = emitters.containsKey(userId);

        // 유저에 대한 emitter 모두 제거
        if (exists) {
            emitters.remove(userId);
        }

    }

    // 특정 유저의 특정 emitter 제거
    public void remove(Long userId, SseEmitter emitter) {

        Set<SseEmitter> userEmitters = emitters.get(userId); // 특정 user Emitter Set

        if (userEmitters != null) {
            userEmitters.remove(emitter); // 특정 emitter 제거
            if (userEmitters.isEmpty()) { // 특정 유저에 대해 더이상 연결 없으면 없으면 ConcurrentHashMap에서도 제거
                emitters.remove(userId);
            }
        }
    }

    // 유저 ID로 모든 emitter 조회
    public Set<SseEmitter> getAll(Long userId) {
        return emitters.getOrDefault(userId, Collections.emptySet());
    }


}
