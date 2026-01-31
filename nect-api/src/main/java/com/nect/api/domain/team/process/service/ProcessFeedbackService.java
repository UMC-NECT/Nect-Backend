package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.FeedbackCreatedByResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackCreateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackDeleteResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackUpdateResDto;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessFeedback;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessFeedbackRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessFeedbackService {

    private final ProcessRepository processRepository;
    private final ProcessFeedbackRepository processFeedbackRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectHistoryPublisher historyPublisher;

    // 헬퍼 메서드
    private void assertWritableMember(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ProcessException(
                    ProcessErrorCode.FORBIDDEN,
                    "프로젝트 멤버가 아닙니다. projectId=" + projectId + ", userId=" + userId
            );
        }
    }

    private Process getActiveProcess(Long projectId, Long processId) {
        return processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));
    }

    private ProcessFeedback getFeedback(Long processId, Long feedbackId) {
        return processFeedbackRepository.findByIdAndProcessIdAndDeletedAtIsNull(feedbackId, processId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.FEEDBACK_NOT_FOUND,
                        "feedbackId=" + feedbackId + ", processId=" + processId
                ));
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ProcessException(ProcessErrorCode.INVALID_FEEDBACK_CONTENT);
        }
    }

    private Map<Long, List<String>> getRoleFieldLabelsMap(Long projectId, Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();

        return projectUserRepository
                .findActiveUserRoleFieldsByProjectIdAndUserIds(projectId, new ArrayList<>(userIds))
                .stream()
                .filter(r -> r.getUserId() != null)
                .collect(Collectors.groupingBy(
                        ProjectUserRepository.UserRoleFieldsRow::getUserId,
                        Collectors.mapping(
                                r -> (r.getRoleField() == RoleField.CUSTOM)
                                        ? "CUSTOM:" + r.getCustomRoleFieldName()
                                        : r.getRoleField().name(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream()
                                                .filter(s -> s != null && !s.isBlank())
                                                .distinct()
                                                .toList()
                                )
                        )
                ));
    }


    // 피드백 생성 서비스
    @Transactional
    public ProcessFeedbackCreateResDto createFeedback(Long projectId, Long userId, Long processId, ProcessFeedbackCreateReqDto req) {
        assertWritableMember(projectId, userId);
        validateContent(req.content());

        Process process = getActiveProcess(projectId, processId);

        User actor = User.builder().userId(userId).build();

        ProcessFeedback feedback = ProcessFeedback.builder()
                .process(process)
                .content(req.content())
                .createdBy(actor)
                .build();

        ProcessFeedback saved = processFeedbackRepository.save(feedback);

        Map<Long, List<String>> roleFieldLabelsMap = getRoleFieldLabelsMap(projectId, List.of(userId));
        List<String> createdByRoleFields = roleFieldLabelsMap.getOrDefault(userId, List.of());

        String createdByUserName = (saved.getCreatedBy() != null) ? saved.getCreatedBy().getName() : null;

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("feedbackId", saved.getId());
        meta.put("content", saved.getContent());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.FEEDBACK_CREATED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );


        // TODO(TEAM EVENT FACADE):
        // - 추후 Notification ActivityFacade(가칭)로 통합하여 activityFacade.recordAndNotify 호출로 변경 예정


        return new ProcessFeedbackCreateResDto(
                saved.getId(),
                saved.getContent(),
                saved.getStatus(),
                new FeedbackCreatedByResDto(userId, createdByUserName, createdByRoleFields),
                saved.getCreatedAt()
        );
    }


    // 피드백 수정
    @Transactional
    public ProcessFeedbackUpdateResDto updateFeedback(Long projectId, Long userId, Long processId, Long feedbackId, ProcessFeedbackUpdateReqDto req) {
        assertWritableMember(projectId, userId);

        validateContent(req.content());

        // 부모 프로세스가 살아있는지 + 프로젝트 소속인지 한 번에 검증
        getActiveProcess(projectId, processId);

        ProcessFeedback feedback = getFeedback(processId, feedbackId);

        String beforeContent = feedback.getContent();
        feedback.updateContent(req.content());
        String afterContent = feedback.getContent();


        // TODO(Notification):
        // - "피드백 수정" 알림 트리거 지점
        // - 수신자: 프로젝트 멤버 전체 OR 해당 프로세스 관련자(assignee/mention) (유저/멤버십 연동 후)
        // - NotificationType 예: PROCESS_FEEDBACK_UPDATED
        // - meta: before/after 또는 "수정됨" 정도만
        // - 권장: AFTER_COMMIT 이후 알림 전송(이벤트 리스너)

        if (!Objects.equals(beforeContent, afterContent)) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("processId", processId);
            meta.put("feedbackId", feedbackId);
            meta.put("before", Map.of("content", beforeContent));
            meta.put("after", Map.of("content", afterContent));

            historyPublisher.publish(
                    projectId,
                    userId,
                    HistoryAction.FEEDBACK_UPDATED,
                    HistoryTargetType.PROCESS,
                    processId,
                    meta
            );
        }


        // TODO(TEAM EVENT FACADE): 추후 ActivityFacade로 통합

        // createdBy 응답 채우기 (User + ProjectUser fieldIds)
        User createdBy = feedback.getCreatedBy();
        Long createdByUserId = (createdBy != null) ? createdBy.getUserId() : null;
        String createdByUserName = (createdBy != null) ? createdBy.getName() : null;

        List<String> createdByRoleFields = List.of();
        if (createdByUserId != null) {
            Map<Long, List<String>> roleFieldLabelsMap = getRoleFieldLabelsMap(projectId, List.of(createdByUserId));
            createdByRoleFields = roleFieldLabelsMap.getOrDefault(createdByUserId, List.of());
        }

        return new ProcessFeedbackUpdateResDto(
                feedback.getId(),
                feedback.getContent(),
                feedback.getStatus(),
                new FeedbackCreatedByResDto(createdByUserId, createdByUserName, createdByRoleFields),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }


    // 피드백 삭제
    @Transactional
    public ProcessFeedbackDeleteResDto deleteFeedback(Long projectId, Long userId, Long processId, Long feedbackId) {
        assertWritableMember(projectId, userId);

        getActiveProcess(projectId, processId);

        ProcessFeedback feedback = getFeedback(processId, feedbackId);

        // TODO(HISTORY/NOTI): 삭제 전 스냅샷 확보 권장
        // - beforeContent = feedback.getContent()
        // - beforeCreatedBy = feedback.getCreatedByUserId() (필드 확정 후)
        // - beforeCreatedAt = feedback.getCreatedAt()

        String beforeContent = feedback.getContent();
        feedback.softDelete();

        // TODO(Notification):
        // - "피드백 삭제" 알림 트리거 지점
        // - 수신자: 프로젝트 멤버 전체 OR 해당 프로세스 관련자
        // - NotificationType 예: PROCESS_FEEDBACK_DELETED
        // - meta: 삭제된 피드백의 content 요약/작성자 등(스냅샷 기반)
        // - 권장: AFTER_COMMIT 이후 알림 전송


        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("feedbackId", feedbackId);
        meta.put("content", beforeContent);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.FEEDBACK_DELETED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        // TODO(TEAM EVENT FACADE): 추후 ActivityFacade로 통합

        return new ProcessFeedbackDeleteResDto(feedbackId);
    }
}
