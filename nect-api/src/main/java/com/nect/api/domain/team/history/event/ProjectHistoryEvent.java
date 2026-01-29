package com.nect.api.domain.team.history.event;



import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;

import java.time.LocalDateTime;

/**
 * 프로젝트 히스토리 저장용 이벤트 (저장은 History 핸들러에서)
 */
public record ProjectHistoryEvent(
        Long projectId,
        Long actorUserId,
        HistoryAction action,
        HistoryTargetType targetType,
        Long targetId,
        String metaJson,
        LocalDateTime occurredAt
) {
    public static ProjectHistoryEvent of(
            Long projectId,
            Long actorUserId,
            HistoryAction action,
            HistoryTargetType targetType,
            Long targetId,
            String metaJson
    ) {
        return new ProjectHistoryEvent(projectId, actorUserId, action, targetType, targetId, metaJson, LocalDateTime.now());
    }
}
