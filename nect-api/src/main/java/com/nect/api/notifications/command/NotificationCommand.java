package com.nect.api.notifications.command;

import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.Project;
import lombok.Builder;
import lombok.Getter;

/**
 * 알림 객체를 생성할 때 과도한 메서드 파라미터 전달을 방지하기 위해
 * Notification 객체 생성에 필요한 파라미터 정보를 담습니다.
 * Builder를 통해 생성할 수 있습니다.
 */
@Getter
@Builder
public class NotificationCommand {

    private final NotificationType type;
    private final NotificationClassification classification;
    private final NotificationScope scope;
    private final Long targetId;
    private final Object[] mainArgs;
    private final Object[] contentArgs;
    private final Project project;

}
