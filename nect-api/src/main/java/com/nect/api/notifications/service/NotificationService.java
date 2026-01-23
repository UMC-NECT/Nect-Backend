package com.nect.api.notifications.service;

import com.nect.api.notifications.command.NotificationCommand;
import com.nect.core.entity.notifications.Notification;
import com.nect.core.entity.user.User;
import com.nect.core.repository.notifications.NotificationRepository;
import lombok.RequiredArgsConstructor;
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

    // 여러 유저에 대해 생성
    @Transactional(readOnly = false)
    public List<Notification> createForUsers(List<User> receivers, NotificationCommand command) {

        // 알림 command에서 Notification 리스트 생성
        List<Notification> notifications = receivers.stream()
                .map(receiver ->
                        Notification.create(
                                command.getType(),
                                command.getClassification(),
                                command.getScope(),
                                command.getTargetId(),
                                receiver,
                                command.getMainArgs(),
                                command.getContentArgs()
                        )
                ).toList();

        // 모든 알림 저장
        return notificationRepository.saveAll(notifications);
    }

    //TODO: 알림 조회

}
