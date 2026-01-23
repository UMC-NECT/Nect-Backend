package com.nect.api.notifications.facade;

import com.nect.api.notifications.command.NotificationCommand;
import com.nect.api.notifications.service.NotificationDispatchService;
import com.nect.api.notifications.service.NotificationService;
import com.nect.core.entity.notifications.Notification;
import com.nect.core.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 전송을 위한 퍼사드(Facade) 계층입니다.
 *
 * NotificationCommand를 기반으로
 * 1) 알림 엔티티를 생성하고
 * 2) 생성된 알림을 실제 전송(SSE, 푸시 등)까지 연결하는
 * 전체 알림 흐름을 조합·조정하는 역할을 담당합니다.
 *
 * 개별 서비스(NotificationService, NotificationDispatchService)의
 * 세부 구현을 숨기고, 상위 계층에서는 단순히
 * "누구에게 어떤 알림을 보낼지"만 전달하면 되도록 합니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;
    private final NotificationDispatchService dispatchService;

    // 여러 유저에 대해 알림 전송
    @Transactional(readOnly = false)
    public void notify(List<User> receivers, NotificationCommand command) {

        // 알림 객체 생성
        List<Notification> notifications = notificationService.createForUsers(receivers, command);

        // 알림 전송
        notifications.forEach(dispatchService::send);

    }

    // 단일 유저에게 알림 전송
    @Transactional(readOnly = false)
    public void notify(User receiver, NotificationCommand command) {
        notify(List.of(receiver), command);
    }

}
