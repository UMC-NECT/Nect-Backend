package com.nect.api.domain.team.history.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.history.event.ProjectHistoryEvent;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProjectHistoryPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    // TODO(인증): Security 붙이면 currentUserId로 교체
    private Long getActorUserId() {
        return 1L;
    }

    public void publish(Long projectId, HistoryAction action, HistoryTargetType targetType, Long targetId, Map<String, Object> meta) {
        eventPublisher.publishEvent(
                ProjectHistoryEvent.of(
                        projectId,
                        getActorUserId(),
                        action,
                        targetType,
                        targetId,
                        toJson(meta)
                )
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
