package com.nect.api.domain.notifications.service;

import com.nect.api.domain.user.exception.UserNotFoundException;
import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.notifications.dto.NotificationListResponse;
import com.nect.api.domain.notifications.enums.code.NotificationErrorCode;
import com.nect.api.domain.notifications.exception.NotificationException;
import com.nect.core.entity.notifications.Notification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.user.User;
import com.nect.core.repository.notifications.NotificationRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림(Notification) 도메인 엔티티를 생성하고
 * 영속화하는 역할을 담당하는 서비스입니다.
 *
 * NotificationCommand를 기반으로 수신자별 알림 엔티티를 생성하며,
 * 알림 전송(SSE, 푸시 등)과 같은 외부 I/O 책임은 포함하지 않습니다.
 *
 * 이 서비스는
 * - 알림 생성 규칙
 * - 알림 저장
 * - 알림 조회
 * 에만 집중하고,
 * 실제 전송은 NotificationDispatchService에서 처리됩니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 여러 유저에 대해 생성
    @Transactional(readOnly = false)
    public List<Notification> createForUsers(List<User> receivers, NotificationCommand command) {

        if (command == null) {
            throw new NotificationException(CommonResponseCode.NULL_POINT_ERROR);
        }

        if (receivers == null || receivers.isEmpty()) {
            throw new NotificationException(CommonResponseCode.BAD_REQUEST_ERROR);
        }

        // 알림 command에서 Notification 리스트 생성
        List<Notification> notifications = receivers.stream()
                .map(receiver ->
                        Notification.create(
                                command.type(),
                                command.classification(),
                                command.scope(),
                                command.targetId(),
                                receiver,
                                command.project(),
                                command.mainArgs(),
                                command.contentArgs()
                        )
                ).toList();

        // 모든 알림 저장
        return notificationRepository.saveAll(notifications);
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(
            Long userId,
            NotificationScope scope,
            Long cursor,
            int size
    ) {

        // 본인 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (scope == null) { // scope 없으면 안됨.
            throw new NotificationException(NotificationErrorCode.INVALID_NOTIFICATION_SCOPE);
        }

        // 페이징 정보
        PageRequest pageRequest = PageRequest.of(0, size);

        // 알림 목록 조회 -> List<Notification> 반환
        List<Notification> notifications = notificationRepository.findByScopeWithCursor(user, scope, cursor, pageRequest);

        // cursor 정보 생성
        Long nextCursor = notifications.isEmpty() ? null : notifications.getLast().getId();

        // API 응답 객체 생성 후 반환
        return NotificationListResponse.from(notifications, nextCursor);
    }



}
