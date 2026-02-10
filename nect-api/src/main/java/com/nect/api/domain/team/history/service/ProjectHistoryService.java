package com.nect.api.domain.team.history.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.history.dto.res.ProjectHistoryItemResDto;
import com.nect.api.domain.team.history.dto.res.ProjectHistoryListResDto;
import com.nect.api.domain.team.history.enums.HistoryErrorCode;
import com.nect.api.domain.team.history.exception.HistoryException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.history.ProjectHistory;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.history.ProjectHistoryRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectHistoryService {


    private static final int PAGE_SIZE = 10;
    private static final int MAX_MAIN_LEN = 80;
    private static final int MAX_CONTENT_LEN = 80;

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;



    private void assertActiveProjectMember(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new HistoryException(
                    HistoryErrorCode.FORBIDDEN,
                    "not an active project member. projectId=" + projectId + ", userId=" + userId
            );
        }
    }

    public ProjectHistoryListResDto getHistories(Long projectId, Long userId, Long cursor) {
        assertActiveProjectMember(projectId, userId);

        // 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new HistoryException(
                        HistoryErrorCode.PROJECT_NOT_FOUND,
                        "projectId=" + projectId
                ));

        // 항상 최근 10개 고정
        PageRequest page = PageRequest.of(0, PAGE_SIZE);

        //  조회
        List<ProjectHistory> histories = (cursor == null)
                ? historyRepository.findLatest(project.getId(), page)
                : historyRepository.findLatestByCursor(project.getId(), cursor, page);

        List<ProjectHistoryItemResDto> items = histories.stream()
                .map(h -> toItemDto(projectId, h))
                .toList();

        // nextCursor 계산
        Long nextCursor = histories.isEmpty() ? null : histories.get(histories.size() - 1).getId();
        return new ProjectHistoryListResDto(nextCursor, items);
    }

    private ProjectHistoryItemResDto toItemDto(Long projectId, ProjectHistory history) {
        ProjectHistoryItemResDto.ActorDto actor = loadActor(projectId, history.getActorUserId());
        String mainMessage = buildMainMessage(history, actor);
        String contentMessage = buildContentMessage(history);

        return new ProjectHistoryItemResDto(
                history.getId(),
                history.getAction(),
                history.getTargetType(),
                history.getTargetId(),
                actor,
                mainMessage,
                contentMessage,
                history.getCreatedAt()
        );
    }

    // 유저 조회
    private ProjectHistoryItemResDto.ActorDto loadActor(Long projectId, Long actorUserId) {
        ProjectUser projectUser = projectUserRepository
                .findByProjectIdAndUserId(projectId, actorUserId)
                .orElseThrow(() -> new HistoryException(
                        HistoryErrorCode.FORBIDDEN,
                        "actor is not an active project member. projectId=" + projectId + ", userId=" + actorUserId
                ));

        var user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new HistoryException(
                        HistoryErrorCode.USER_NOT_FOUND,
                        "userId=" + actorUserId
                ));

        return new ProjectHistoryItemResDto.ActorDto(
                actorUserId,
                user.getName(),
                user.getNickname(),
                projectUser.getRoleField(),
                projectUser.getCustomRoleFieldName()
        );
    }



    private String buildMainMessage(ProjectHistory history, ProjectHistoryItemResDto.ActorDto actor) {
        HistoryAction action = history.getAction();
        JsonNode meta = parseMeta(history.getMetaJson());

        return switch (action) {
            case WEEK_MISSION_STATUS_CHANGED,
                 WEEK_MISSION_TASK_ITEM_UPDATED,
                 WEEK_MISSION_TASK_ITEM_REORDERED -> {
                int missionNumber = intValue(meta, "missionNumber");
                yield action.formatMainMessage(MAX_MAIN_LEN, missionNumber);
            }

            case PROCESS_CREATED, PROCESS_UPDATED -> {
                // (프로세스 제목) 프로세스가 생성/수정되었습니다.
                String processTitle = text(meta, "processTitle", "title");
                yield action.formatMainMessage(MAX_MAIN_LEN, processTitle);
            }
            case PROCESS_FEEDBACK_CREATED, PROCESS_FEEDBACK_UPDATED, PROCESS_FEEDBACK_DELETED -> {
                // (프로세스 제목)에서 피드백이 생성되었습니다.
                String processTitle = text(meta, "processTitle", "title");
                yield action.formatMainMessage(MAX_MAIN_LEN, processTitle);
            }
            default -> action.formatMainMessage(MAX_MAIN_LEN);
        };
    }

    private String buildContentMessage(ProjectHistory history) {
        HistoryAction action = history.getAction();
        if (!action.hasContent()) return null;

        JsonNode meta = parseMeta(history.getMetaJson());

        return switch (action) {
            case WEEK_MISSION_STATUS_CHANGED -> {
                String processTitle = text(meta, "processTitle", "title");
                yield action.formatContentMessage(MAX_CONTENT_LEN, processTitle);
            }

            case WEEK_MISSION_TASK_ITEM_UPDATED -> {
                String preview = text(meta, "taskItemPreview", "processTitle", "title");
                yield action.formatContentMessage(MAX_CONTENT_LEN, preview);
            }

            case WEEK_MISSION_TASK_ITEM_REORDERED -> {
                String processTitle = text(meta, "processTitle", "title");
                yield action.formatContentMessage(MAX_CONTENT_LEN, processTitle);
            }

            case PROCESS_FEEDBACK_CREATED, PROCESS_FEEDBACK_UPDATED, PROCESS_FEEDBACK_DELETED -> {
                // “피드백 내용”
                String feedbackContent = text(meta, "feedbackContent", "content");
                yield action.formatContentMessage(MAX_CONTENT_LEN, feedbackContent);
            }
            default -> null;
        };
    }

    private JsonNode parseMeta(String raw) {
        if (raw == null || raw.isBlank()) return objectMapper.createObjectNode();
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    private static String text(JsonNode node, String... keys) {
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && !v.isNull()) {
                String s = v.asText();
                if (s != null && !s.isBlank()) return s;
            }
        }
        return "";
    }

    private static int intValue(JsonNode node, String key) {
        JsonNode v = node.get(key);
        return (v == null || v.isNull()) ? 0 : v.asInt();
    }
}
