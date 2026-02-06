package com.nect.api.domain.notifications.enums.code;

import com.nect.core.entity.notifications.enums.NotificationScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 검색 조회 필터에 사용됨
 */
@Getter
@AllArgsConstructor
public enum NotificationSearchFilter {

    EXPLORATION(List.of(NotificationScope.MAIN_HOME)),
    WORKSPACE_ONLY(List.of(NotificationScope.WORKSPACE_ONLY)),
    WORKSPACE_GLOBAL(List.of(NotificationScope.WORKSPACE_GLOBAL)),
    WORKSPACES(List.of(NotificationScope.WORKSPACE_GLOBAL, NotificationScope.WORKSPACE_ONLY))

    ;

    private final List<NotificationScope> scopes;

}
