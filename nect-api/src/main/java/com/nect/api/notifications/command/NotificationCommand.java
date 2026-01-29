package com.nect.api.notifications.command;

import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.Project;

/**
 * 알림 객체를 생성할 때 과도한 메서드 파라미터 전달을 방지하기 위해
 * Notification 객체 생성에 필요한 파라미터 정보를 담습니다.
 */
public record NotificationCommand(
        NotificationType type,
        NotificationClassification classification,
        NotificationScope scope,
        Long targetId,
        Object[] mainArgs,
        Object[] contentArgs,
        Project project
)
{}
