package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.ProcessTaskItemReorderReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessTaskItemUpsertReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemReorderResDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemResDto;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessTaskItem;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.team.process.ProcessTaskItemRepository;
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

    // TODO : 알림 & History 추가 예정
//    private final NotificationFacade notificationFacade;

    // TODO(인증/인가): Security/User 붙이면 CurrentUserProvider(또는 AuthFacade), ProjectUserRepository(멤버십 검증용) 주입 예정

    private final ProjectHistoryPublisher historyPublisher;

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

    // 항목 생성 서비스
    @Transactional
    public ProcessTaskItemResDto create(Long projectId, Long processId, ProcessTaskItemUpsertReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 task item 생성 가능)

        Process process = getActiveProcess(projectId, processId);

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

        normalizeSortOrder(processId);

        Long actorUserId = 1L; // TODO(인증): 현재 로그인 유저
        // TODO(Notification):
        // - 프로젝트 멤버 전체 또는 해당 프로세스 관련자(assignee/mention)에게 "업무 항목 추가" 알림 전송
        // - 유저/멤버십 붙으면 NotificationFacade 주입 후 notify 호출
        // - 권장: AFTER_COMMIT 이벤트로 보내기
        // notifyProjectMembersTodo(projectId, actorUserId, processId, "TASK_ITEM_CREATED");
        // notifyMentionsTodo(projectId, actorUserId, processId, /* process mention ids */);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("taskItemId", saved.getId());
        meta.put("content", saved.getContent());
        meta.put("isDone", saved.isDone());
        meta.put("sortOrder", saved.getSortOrder()); // normalize 이후 최종 값

        historyPublisher.publish(
                projectId,
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
    public ProcessTaskItemResDto update(Long projectId, Long processId, Long taskItemId, ProcessTaskItemUpsertReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증

        getActiveProcess(projectId, processId);

        ProcessTaskItem item = getTaskItem(processId, taskItemId);

        // 변경 감지용 before 스냅샷
        String beforeContent = item.getContent();
        boolean beforeDone = item.isDone();
        Integer beforeOrder = item.getSortOrder();

        boolean changed = false;

        if (req.content() != null) {
            if (req.content().isBlank())
                throw new ProcessException(ProcessErrorCode.INVALID_TASK_ITEM_CONTENT);
            if (!req.content().equals(beforeContent)) {
                item.updateContent(req.content());
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
            Long actorUserId = 1L; // TODO(인증)

            // TODO(Notification):
            // - 유저/멤버십 연동 후 수신자 결정(프로젝트 멤버 / 해당 프로세스 assignee / mention 등)
            // - "업무 항목 수정" 알림 전송
            // - meta에 변경 요약 포함 권장(예: done 토글, 내용 변경, 순서 변경)
            // - 권장: AFTER_COMMIT 이벤트 리스너로 전송
            // notifyTaskItemUpdatedTodo(projectId, actorUserId, processId, item.getId(), ...);

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
    public void delete(Long projectId, Long processId, Long taskItemId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증

        getActiveProcess(projectId, processId);

        ProcessTaskItem item = getTaskItem(processId, taskItemId);

        // TODO(HISTORY/NOTI): 삭제 전 스냅샷(내용/완료여부/정렬순서) 확보

        String beforeContent = item.getContent();
        boolean beforeDone = item.isDone();
        Integer beforeOrder = item.getSortOrder();

        item.softDelete();

        normalizeSortOrder(processId);

        // TODO(Notification/ HISTORY):
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("taskItemId", taskItemId);
        meta.put("content", beforeContent);
        meta.put("isDone", beforeDone);
        meta.put("sortOrder", beforeOrder);

        historyPublisher.publish(
                projectId,
                HistoryAction.TASK_ITEM_DELETED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        // TODO(TEAM EVENT FACADE): 추후 ActivityFacade로 통합
    }

    // 업무 위치 변경 서비스
    @Transactional
    public ProcessTaskItemReorderResDto reorder(Long projectId, Long processId, ProcessTaskItemReorderReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 reorder 가능)

        List<Long> orderedIds = req.orderedTaskItemIds();

        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST,
                    "ordered_task_item_ids is empty"
            );
        }

        // 중복 체크
        Set<Long> uniqItems = new HashSet<>(orderedIds);
        if(uniqItems.size() != orderedIds.size()) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "ordered_task_item_ids contains duplicates"
            );
        }

        // 프로젝트 소속 검증
        getActiveProcess(projectId, processId);

        // 혹시 기존 sortOrder가 꼬여있어도 안전하게 시작
        normalizeSortOrder(processId);

        // TODO(NOTI): before orderedIds 스냅샷이 필요하면 여기서 조회

        List<Long> beforeIds = taskItemRepository
                .findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(processId)
                .stream().map(ProcessTaskItem::getId).toList();


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

        // TODO(Notification):

        if (!beforeIds.equals(orderedIds)) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("processId", processId);
            meta.put("beforeOrderedTaskItemIds", beforeIds);
            meta.put("afterOrderedTaskItemIds", orderedIds);

            historyPublisher.publish(
                    projectId,
                    HistoryAction.TASK_ITEM_REORDERED,
                    HistoryTargetType.PROCESS,
                    processId,
                    meta
            );
        }



        // TODO(TEAM EVENT FACADE): 추후 ActivityFacade로 통합

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
