package com.nect.api.domain.team.process.service;

import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.FeedbackCreatedByResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackCreateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackDeleteResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackUpdateResDto;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessFeedback;
import com.nect.core.entity.team.process.ProcessUser;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessFeedbackRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessFeedbackService {

    private final ProcessRepository processRepository;
    private final ProcessFeedbackRepository processFeedbackRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;

    private final NotificationFacade notificationFacade;
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

    //- 해당 프로세스 담당자(assignee)들에게 보냄
    private List<Long> getAssigneeUserIdsFromProcess(Process process) {
        if (process == null || process.getProcessUsers() == null) return List.of();

       return process.getProcessUsers().stream()
               .map(ProcessUser::getUser)
               .filter(Objects::nonNull)
               .map(User::getUserId)
               .filter(Objects::nonNull)
               .distinct()
               .toList();
    }

    private String preview(String text, int max) {
        if(text == null) return null;
        String t = text.trim();
        if(t.length() <= max) return t;
        return t.substring(0, max) + "...";
    }

    private void notifyAfterCommit(List<User> receivers, NotificationCommand notificationCommand) {
        if(receivers == null || receivers.isEmpty()) return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationFacade.notify(receivers, notificationCommand);
            }
        });
    }

    // 피드백 생성 서비스
    @Transactional
    public ProcessFeedbackCreateResDto createFeedback(Long projectId, Long userId, Long processId, ProcessFeedbackCreateReqDto req) {
        assertWritableMember(projectId, userId);
        validateContent(req.content());

        Process process = getActiveProcess(projectId, processId);

        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.USER_NOT_FOUND, "userId=" + userId));

        ProcessFeedback feedback = ProcessFeedback.builder()
                .process(process)
                .content(req.content())
                .createdBy(actor)
                .build();

        ProcessFeedback saved = processFeedbackRepository.save(feedback);

        Map<Long, List<String>> roleFieldLabelsMap = getRoleFieldLabelsMap(projectId, List.of(userId));
        List<String> createdByRoleFields = roleFieldLabelsMap.getOrDefault(userId, List.of());

        String createdByUserName = (saved.getCreatedBy() != null) ? saved.getCreatedBy().getName() : null;
        String createdByNickname = (saved.getCreatedBy() != null) ? saved.getCreatedBy().getNickname() : null;

        // 히스토리
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


        // 알림 (새 피드백)
        // 수신자 : 담당자(assignees) -> 작성자는 제외
        List<Long> assignneIds = getAssigneeUserIdsFromProcess(process);
        List<Long> receiverIds = assignneIds.stream()
                .filter(id -> id != null && !id.equals(userId))
                .distinct()
                .toList();

        if(!receiverIds.isEmpty()) {
            List<User> receivers = userRepository.findAllById(receiverIds);

            Project project = process.getProject();
            NotificationCommand command = new NotificationCommand(
                    NotificationType.WORKSPACE_TASK_FEEDBACK,
                    NotificationClassification.WORK_STATUS,
                    NotificationScope.WORKSPACE_ONLY,
                    processId,
                    new Object[]{actor.getName()},
                    new Object[]{preview(saved.getContent(), 60)},
                    project
            );

            // 커밋 이후 전송
            notifyAfterCommit(receivers, command);
        }


        return new ProcessFeedbackCreateResDto(
                saved.getId(),
                saved.getContent(),
                saved.getStatus(),
                new FeedbackCreatedByResDto(userId, createdByUserName, createdByNickname, createdByRoleFields),
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

        // createdBy 응답 채우기 (User + ProjectUser fieldIds)
        User createdBy = feedback.getCreatedBy();
        Long createdByUserId = (createdBy != null) ? createdBy.getUserId() : null;
        String createdByUserName = (createdBy != null) ? createdBy.getName() : null;
        String createdByNickname = (createdBy != null) ? createdBy.getNickname() : null;

        List<String> createdByRoleFields = List.of();
        if (createdByUserId != null) {
            Map<Long, List<String>> roleFieldLabelsMap = getRoleFieldLabelsMap(projectId, List.of(createdByUserId));
            createdByRoleFields = roleFieldLabelsMap.getOrDefault(createdByUserId, List.of());
        }

        return new ProcessFeedbackUpdateResDto(
                feedback.getId(),
                feedback.getContent(),
                feedback.getStatus(),
                new FeedbackCreatedByResDto(createdByUserId, createdByUserName, createdByNickname, createdByRoleFields),
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

        String beforeContent = feedback.getContent();
        feedback.softDelete();


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

        return new ProcessFeedbackDeleteResDto(feedbackId);
    }
}
