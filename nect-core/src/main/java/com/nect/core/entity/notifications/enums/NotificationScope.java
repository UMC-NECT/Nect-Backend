package com.nect.core.entity.notifications.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * 알림이 표시되는 범위를 설정하는 enum입니다.
 *
 */
@Getter
@AllArgsConstructor
public enum NotificationScope {

    MAIN_HOME("main_home"), // 메인화면에만 뜨는 알림
    WORKSPACE_ONLY("workspace_only"), // 작업실에서 발생하는 알림 -> 작업실에서만 표시하는 알림
    WORKSPACE_GLOBAL("workspace_global"), // 작업실에서 발생하는 알림 -> 메인화면 + 작업실에서 표시하는 알림

    ;

    private final String eventName;


}
