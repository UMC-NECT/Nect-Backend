package com.nect.api.domain.team.process.service;

import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.ProcessTaskItemReorderReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessTaskItemUpsertReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemReorderResDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemResDto;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessTaskItem;
import com.nect.core.entity.team.process.enums.ProcessType;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.team.process.ProcessTaskItemRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessTaskItemService {
    private final ProcessRepository processRepository;
    private final ProcessTaskItemRepository taskItemRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectHistoryPublisher historyPublisher;

    private final UserRepository userRepository;
    private final NotificationFacade notificationFacade;

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

    private ProcessTaskItem getTaskItem(Long processId, Long taskItemId) {
        return taskItemRepository.findByIdAndProcessIdAndDeletedAtIsNull(taskItemId, processId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.TASK_ITEM_NOT_FOUND,
                        "taskItemId=" + taskItemId + ", processId=" + processId
                ));
    }

    // 전체 정규화
    private void normalizeSortOrder(Long processId) {
        List<ProcessTaskItem> items =
                taskItemRepository.findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(processId);

        items = items.stream()
                .sorted(Comparator.comparing(t -> t.getSortOrder() == null ? Integer.MAX_VALUE : t.getSortOrder()))
                .toList();

        int i = 0;
        for (ProcessTaskItem it : items) {
            it.updateSortOrder(i++);
        }
    }

    // 파트별 정규화
    private void normalizeSortOrderByGroup(Long processId, RoleField roleField, String customName) {
        List<ProcessTaskItem> items = taskItemRepository
                .findAllByProcessIdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldNameOrderBySortOrderAsc(
                        processId, roleField, customName
                );

        items = items.stream()
                .sorted(Comparator.comparing(t -> t.getSortOrder() == null ? Integer.MAX_VALUE : t.getSortOrder()))
                .toList();

        int i = 0;
        for (ProcessTaskItem it : items) {
            it.updateSortOrder(i++);
        }
    }

    private void assertWeekMissionLeader(Long projectId, Long userId) {
        boolean isLeader = projectUserRepository
                .existsByProjectIdAndUserIdAndMemberTypeAndMemberStatus(projectId, userId, ProjectMemberType.LEADER, ProjectMemberStatus.ACTIVE);

        if (!isLeader) {
            throw new ProcessException(
                    ProcessErrorCode.FORBIDDEN,
                    "WEEK_MISSION은 프로젝트 리더만 수정할 수 있습니다. projectId=" + projectId + ", userId=" + userId
            );
        }
    }

    private void assertReorderPermission(Long projectId, Long userId, Process process) {
        if (process.getProcessType() == ProcessType.WEEK_MISSION) {
            assertWeekMissionLeader(projectId, userId);
            return;
        }
        assertWritableMember(projectId, userId);
    }


    // 항목 생성 서비스
    @Transactional
    public ProcessTaskItemResDto create(Long projectId, Long userId, Long processId, ProcessTaskItemUpsertReqDto req) {
        assertWritableMember(projectId, userId);

        Process process = getActiveProcess(projectId, processId);

        // 위크미션 TASK 수정 권한(리더만 가능)
        assertReorderPermission(projectId, userId, process);

        if (req.content() == null || req.content().isBlank()) {
            throw new ProcessException(ProcessErrorCode.INVALID_TASK_ITEM_CONTENT);
        }

        List<ProcessTaskItem> items = taskItemRepository.findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(processId);

        int size = items.size();
        int insertOrder;
        if (req.sortOrder() == null) insertOrder = size;
        else if (req.sortOrder() < 0) insertOrder = 0;
        else insertOrder = Math.min(req.sortOrder(), size);

        for (ProcessTaskItem it : items) {
            Integer o = it.getSortOrder();
            if (o == null) continue;
            if (o >= insertOrder) it.updateSortOrder(o + 1);
        }

        ProcessTaskItem newItem = ProcessTaskItem.builder()
                .process(process)
                .content(req.content())
                .isDone(Boolean.TRUE.equals(req.isDone()))
                .sortOrder(insertOrder)
                .build();

        ProcessTaskItem saved = taskItemRepository.save(newItem);

        // 최종 정규화
        normalizeSortOrder(processId);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("taskItemId", saved.getId());
        meta.put("content", saved.getContent());
        meta.put("isDone", saved.isDone());
        meta.put("sortOrder", saved.getSortOrder());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.TASK_ITEM_CREATED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        return new ProcessTaskItemResDto(
                saved.getId(),
                saved.getContent(),
                saved.isDone(),
                saved.getSortOrder(),
                saved.getDoneAt()
        );
    }

    // 업무 항목 수정
    @Transactional
    public ProcessTaskItemResDto update(Long projectId, Long userId, Long processId, Long taskItemId, ProcessTaskItemUpsertReqDto req) {
        assertWritableMember(projectId, userId);

        getActiveProcess(projectId, processId);

        ProcessTaskItem item = getTaskItem(processId, taskItemId);

        // 변경 감지용 before 스냅샷
        String beforeContent = item.getContent();
        boolean beforeDone = item.isDone();
        Integer beforeOrder = item.getSortOrder();

        boolean changed = false;

        if (req == null) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "request is null");
        }

        if (req.content() != null) {
            if (req.content().isBlank()) {
                throw new ProcessException(ProcessErrorCode.INVALID_TASK_ITEM_CONTENT, "content is blank");
            }
            String afterContent = req.content().trim();
            if (!Objects.equals(beforeContent, afterContent)) {
                item.updateContent(afterContent);
                changed = true;
            }
        }

        if (req.isDone() != null) {
            boolean afterDone = Boolean.TRUE.equals(req.isDone());
            if (afterDone != beforeDone) {
                item.updateDone(afterDone);
                changed = true;
            }
        }

        // sort_order 수정
        if (req.sortOrder() != null) {
            // 항상 현재 상태를 먼저 정규화해서 null/중복 order 제거
            normalizeSortOrder(processId);

            List<ProcessTaskItem> items = taskItemRepository.findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(processId);

            // 현재 인덱스/목표 인덱스 계산
            int currentIndex = item.getSortOrder();
            int maxIndex = Math.max(items.size() - 1, 0);

            int targetIndex = req.sortOrder();
            if (targetIndex < 0) targetIndex = 0;
            if (targetIndex > maxIndex) targetIndex = maxIndex;

            // 이동 필요할 때만 리스트 재배치 후 0..n-1 다시 부여
            if (targetIndex != currentIndex) {
                // items는 sortOrder ASC라 index==sortOrder
                items.remove(currentIndex);
                items.add(targetIndex, item);

                for (int i = 0; i < items.size(); i++) {
                    items.get(i).updateSortOrder(i);
                }
                changed = true;
            }
        }


        if (changed) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("processId", processId);
            meta.put("taskItemId", item.getId());
            meta.put("before", Map.of(
                    "content", beforeContent,
                    "isDone", beforeDone,
                    "sortOrder", beforeOrder
            ));
            meta.put("after", Map.of(
                    "content", item.getContent(),
                    "isDone", item.isDone(),
                    "sortOrder", item.getSortOrder()
            ));

            historyPublisher.publish(
                    projectId,
                    userId,
                    HistoryAction.TASK_ITEM_UPDATED,
                    HistoryTargetType.PROCESS,
                    processId,
                    meta
            );
        }

        return new ProcessTaskItemResDto(
                item.getId(),
                item.getContent(),
                item.isDone(),
                item.getSortOrder(),
                item.getDoneAt()
        );
    }



    // 업무 항목 삭제
    @Transactional
    public void delete(Long projectId, Long userId, Long processId, Long taskItemId) {
        assertWritableMember(projectId, userId);

        getActiveProcess(projectId, processId);

        ProcessTaskItem item = getTaskItem(processId, taskItemId);

        // Before 스냅샷
        String beforeContent = item.getContent();
        boolean beforeDone = item.isDone();
        Integer beforeOrder = item.getSortOrder();

        item.softDelete();

        normalizeSortOrder(processId);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("taskItemId", taskItemId);
        meta.put("content", beforeContent);
        meta.put("isDone", beforeDone);
        meta.put("sortOrder", beforeOrder);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.TASK_ITEM_DELETED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

    }

    private List<User> loadWorkspaceReceivers(Long projectId, Long actorId) {
        return projectUserRepository.findAllUsersByProjectId(projectId).stream()
                .filter(u -> !u.getUserId().equals(actorId))
                .toList();
    }

    private void notifyWorkspaceWeekMissionUpdated(Process process, Long actorId) {
        // WEEK_MISSION만 알림
        if (process.getProcessType() != com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION) return;

        Project project = process.getProject();
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.USER_NOT_FOUND, "userId=" + actorId));

        List<User> receivers = loadWorkspaceReceivers(project.getId(), actor.getUserId());
        if (receivers.isEmpty()) return;

        NotificationCommand command = new NotificationCommand(
                NotificationType.WORKSPACE_MISSION_UPDATED,
                NotificationClassification.WORK_STATUS,
                NotificationScope.WORKSPACE_ONLY,
                process.getId(),
                new Object[]{ process.getMissionNumber() },
                new Object[]{ process.getTitle() },
                project
        );

        notificationFacade.notify(receivers, command);
    }

    // 업무 위치 변경 서비스 (멤버형, 리더형을 하나로 관리)
    @Transactional
    public ProcessTaskItemReorderResDto reorder(Long projectId, Long userId, Long processId, ProcessTaskItemReorderReqDto req) {
        Process process = getActiveProcess(projectId, processId);

        assertReorderPermission(projectId, userId, process);

        if (req == null || req.orderedTaskItemIds() == null || req.orderedTaskItemIds().isEmpty()) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "ordered_task_item_ids is empty");
        }

        List<Long> orderedIds = req.orderedTaskItemIds().stream()
                .filter(Objects::nonNull)
                .toList();

        if (orderedIds.isEmpty()) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "ordered_task_item_ids is empty");
        }

        if (new HashSet<>(orderedIds).size() != orderedIds.size()) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "ordered_task_item_ids contains duplicates");
        }

        // 위크미션 TASK내에 필드별 항목 리스트 / 전체(멤버형)
        RoleField roleField = req.roleField();
        String customName = req.customRoleFieldName();

        boolean groupMode = (roleField != null);

        if (groupMode) {
            // 분야별 모드 유효성 검사
            if (roleField == RoleField.CUSTOM) {
                if (customName == null || customName.isBlank()) {
                    throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "CUSTOM이면 custom_role_field_name 필수");
                }
                customName = customName.trim();
            } else {
                // CUSTOM이 아니면 null로 고정
                customName = null;
            }

            // 분야별 정규화(꼬임 방지)
            normalizeSortOrderByGroup(processId, roleField, customName);

            // beforeIds (분야별)
            List<ProcessTaskItem> groupAll = taskItemRepository
                    .findAllByProcessIdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldNameOrderBySortOrderAsc(
                            processId, roleField, customName
                    );

            List<Long> beforeIds = groupAll.stream().map(ProcessTaskItem::getId).toList();

            // 변경 없으면 그대로 반환
            if (beforeIds.equals(orderedIds)) {
                List<ProcessTaskItemResDto> resItems = groupAll.stream()
                        .map(t -> new ProcessTaskItemResDto(t.getId(), t.getContent(), t.isDone(), t.getSortOrder(), t.getDoneAt()))
                        .toList();
                return new ProcessTaskItemReorderResDto(processId, resItems);
            }

            // 전체 포함 정책(그룹 단위)
            if (groupAll.size() != orderedIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_task_item_ids must include all task items of the group"
                );
            }

            // 요청 ids가 모두 해당 그룹의 항목인지 검증
            List<ProcessTaskItem> targets =
                    taskItemRepository.findAllByProcessIdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldNameAndIdIn(
                            processId, roleField, customName, orderedIds
                    );

            if (targets.size() != orderedIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_task_item_ids contains invalid taskItemId(s) for the group"
                );
            }

            Map<Long, ProcessTaskItem> map = targets.stream()
                    .collect(Collectors.toMap(ProcessTaskItem::getId, t -> t));

            // 재정렬 반영(분야별 그룹 내부 0..n-1)
            int i = 0;
            for (Long id : orderedIds) {
                ProcessTaskItem item = map.get(id);
                item.updateSortOrder(i++);
            }

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("processId", processId);
            meta.put("processType", process.getProcessType() == null ? null : process.getProcessType().name());
            meta.put("missionNumber", process.getMissionNumber());
            meta.put("title", process.getTitle());

            meta.put("groupMode", true);
            meta.put("roleField", roleField.name());
            meta.put("customRoleFieldName", customName);

            meta.put("beforeOrderedTaskItemIds", beforeIds);
            meta.put("afterOrderedTaskItemIds", orderedIds);

            historyPublisher.publish(
                    projectId,
                    userId,
                    HistoryAction.TASK_ITEM_REORDERED,
                    HistoryTargetType.PROCESS,
                    processId,
                    meta
            );

            notifyWorkspaceWeekMissionUpdated(process, userId);

            // 응답(요청 순서대로)
            List<ProcessTaskItemResDto> resItems = orderedIds.stream()
                    .map(id -> {
                        ProcessTaskItem t = map.get(id);
                        return new ProcessTaskItemResDto(
                                t.getId(), t.getContent(), t.isDone(), t.getSortOrder(), t.getDoneAt()
                        );
                    })
                    .toList();

            return new ProcessTaskItemReorderResDto(processId, resItems);
        }

        /*
         * 멤버형 프로세스 모달 전용
         * */

        // 꼬임 방지용 정규화
        normalizeSortOrder(processId);

        List<Long> beforeIds = taskItemRepository
                .findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(processId)
                .stream()
                .map(ProcessTaskItem::getId)
                .toList();

        // 변경 없으면 그대로 반환
        if (beforeIds.equals(orderedIds)) {
            List<ProcessTaskItemResDto> resItems = taskItemRepository
                    .findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(processId)
                    .stream()
                    .map(t -> new ProcessTaskItemResDto(
                            t.getId(), t.getContent(), t.isDone(), t.getSortOrder(), t.getDoneAt()
                    ))
                    .toList();

            return new ProcessTaskItemReorderResDto(processId, resItems);
        }


        // 현재 전체 항목
        List<ProcessTaskItem> all = taskItemRepository.findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(processId);

        // 전체 포함 정책
        if(all.size() != orderedIds.size()) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "ordered_task_item_ids must include all task items of the process"
            );
        }

        // 요청 id들이 모두 이 프로세스의 항목인지 검증
        List<ProcessTaskItem> targets =
                taskItemRepository.findAllByProcessIdAndDeletedAtIsNullAndIdIn(processId, orderedIds);
        if(targets.size() != orderedIds.size()) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "ordered_task_item_ids contains invalid taskItemId(s)"
            );
        }

        Map<Long, ProcessTaskItem> map = targets.stream()
                .collect(Collectors.toMap(ProcessTaskItem::getId, t -> t));

        // 재정렬 반영
        int i = 0;
        for (Long id : orderedIds) {
            ProcessTaskItem item = map.get(id);
            item.updateSortOrder(i++);
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("processType", process.getProcessType() == null ? null : process.getProcessType().name());
        meta.put("missionNumber", process.getMissionNumber());
        meta.put("title", process.getTitle());

        meta.put("groupMode", false);
        meta.put("beforeOrderedTaskItemIds", beforeIds);
        meta.put("afterOrderedTaskItemIds", orderedIds);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.TASK_ITEM_REORDERED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        notifyWorkspaceWeekMissionUpdated(process, userId);


        List<ProcessTaskItemResDto> resItems = orderedIds.stream()
                .map(id -> {
                    ProcessTaskItem t = map.get(id);
                    return new ProcessTaskItemResDto(
                            t.getId(),
                            t.getContent(),
                            t.isDone(),
                            t.getSortOrder(),
                            t.getDoneAt()
                    );
                })
                .toList();

        return new ProcessTaskItemReorderResDto(processId, resItems);
    }



}
