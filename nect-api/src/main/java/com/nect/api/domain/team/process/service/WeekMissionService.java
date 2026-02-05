package com.nect.api.domain.team.process.service;

import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.WeekMissionStatusUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.WeekMissionTaskItemUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemResDto;
import com.nect.api.domain.team.process.dto.res.WeekMissionDetailResDto;
import com.nect.api.domain.team.process.dto.res.WeekMissionWeekResDto;
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
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.team.process.ProcessTaskItemRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeekMissionService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;
    private final ProcessTaskItemRepository processTaskItemRepository;

    private final UserRepository userRepository;
    private final NotificationFacade notificationFacade;
    private final ProjectHistoryPublisher historyPublisher;

    private void assertActiveProjectMember(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ProcessException(
                    ProcessErrorCode.FORBIDDEN,
                    "프로젝트 멤버가 아닙니다. projectId=" + projectId + ", userId=" + userId
            );
        }
    }

    private void assertActiveLeader(Long projectId, Long userId) {
        boolean ok = projectUserRepository.existsByProjectIdAndUserIdAndMemberTypeAndMemberStatus(
                projectId, userId, ProjectMemberType.LEADER, ProjectMemberStatus.ACTIVE
        );
        if (!ok) {
            throw new ProcessException(ProcessErrorCode.FORBIDDEN, "WEEK_MISSION 수정은 프로젝트 리더만 가능합니다.");
        }
    }

    private String normalizeCustom(RoleField roleField, String customName) {
        if (roleField == RoleField.CUSTOM) {
            if (customName == null || customName.isBlank()) {
                throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "CUSTOM이면 custom_role_field_name 필수");
            }
            return customName.trim();
        }
        return null;
    }

    private void normalizeGroupOrders(Long processId, RoleField roleField, String customName) {
        List<ProcessTaskItem> items = processTaskItemRepository
                .findWeekMissionGroupItemsOrdered(processId, roleField, customName);

        int i = 0;
        for (ProcessTaskItem it : items) {
            it.updateSortOrder(i++);
        }
    }

    private void assertProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProcessException(ProcessErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId);
        }
    }

    private Integer calcLeftDay(LocalDate deadLine) {
        if (deadLine == null) return null;
        long diff = ChronoUnit.DAYS.between(LocalDate.now(), deadLine);
        return (int) Math.max(diff, 0);
    }


    private WeekMissionWeekResDto toWeekRes(
            LocalDate start,
            LocalDate end,
            List<ProcessRepository.WeekMissionCardRow> rows,
            WeekMissionWeekResDto.AssigneeProfileDto leaderFallback
    ){
        List<WeekMissionWeekResDto.WeekMissionCardResDto> cards = rows.stream()
                .map(r -> {
                    long done = (r.getDoneCount() == null) ? 0L : r.getDoneCount();
                    long total = (r.getTotalCount() == null) ? 0L : r.getTotalCount();

                    WeekMissionWeekResDto.AssigneeProfileDto assignee =
                            (r.getLeaderUserId() != null)
                                    ? new WeekMissionWeekResDto.AssigneeProfileDto(
                                    r.getLeaderUserId(),
                                    r.getLeaderNickname(),
                                    r.getLeaderProfileImageUrl()
                            )
                                    : leaderFallback;

                    return new WeekMissionWeekResDto.WeekMissionCardResDto(
                            r.getProcessId(),
                            r.getMissionNumber(),
                            r.getStatus(),
                            r.getTitle(),
                            r.getStartDate(),
                            r.getDeadLine(),
                            calcLeftDay(r.getDeadLine()),
                            (int) done,
                            (int) total,
                            assignee
                    );
                })
                .toList();

        return new WeekMissionWeekResDto(start, end, cards);
    }

    private List<User> loadWorkspaceReceivers(Long projectId, Long actorId) {
        return projectUserRepository.findAllUsersByProjectId(projectId).stream()
                .filter(u -> !u.getUserId().equals(actorId))
                .toList();
    }

    private void notifyWorkspaceWeekMissionUpdated(Project project, User actor, Process process) {
        List<User> receivers = loadWorkspaceReceivers(project.getId(), actor.getUserId());
        if (receivers == null || receivers.isEmpty()) return;

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

    private void publishWeekMissionHistory(
            Long projectId, Long userId, Long processId,
            HistoryAction action,
            Map<String, Object> meta
    ) {
        historyPublisher.publish(
                projectId,
                userId,
                action,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );
    }

    /**
     * 주차(월~일) 기준 WEEK_MISSION 목록 (정규화 O)
     * GET /week-missions/week?start_date=YYYY-MM-DD&weeks=4
     */
    @Transactional(readOnly = true)
    public WeekMissionWeekResDto getWeekMissions(Long projectId, Long userId, LocalDate startDate, Integer weeks) {
        assertActiveProjectMember(projectId, userId);
        assertProjectExists(projectId);

        int w = (weeks == null) ? 1 : weeks;

        // 방어 (원하는 상한 정하면 됨)
        if (w <= 0) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "weeks must be >= 1");
        }
        if (w > 12) { // 예: 과도 조회 방지
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "weeks is too large");
        }

        LocalDate fallback = processRepository.findMinWeekMissionStartAt(projectId);
        if (fallback == null) {
            // 프로젝트에 아직 위크미션이 없다면(혹은 startAt이 전부 null)
            fallback = LocalDate.now();
        }

        LocalDate weekStart = resolveWeekStart(startDate, fallback);
        LocalDate end = weekStart.plusDays(w * 7L - 1);

        var rows = processRepository.findWeekMissionCardsInRange(projectId, weekStart, end);

        WeekMissionWeekResDto.AssigneeProfileDto leaderFallback = projectUserRepository
                .findActiveLeaderProfile(projectId)
                .map(r -> new WeekMissionWeekResDto.AssigneeProfileDto(
                        r.getUserId(),
                        r.getNickname(),
                        r.getProfileImageUrl()
                ))
                .orElse(null);

        return toWeekRes(weekStart, end, rows, leaderFallback);
    }

    /**
     * WEEK_MISSION 상세 (체크리스트 포함)
     * GET /week-missions/{processId}
     */
    @Transactional(readOnly = true)
    public WeekMissionDetailResDto getDetail(Long projectId, Long userId, Long processId) {
        assertActiveProjectMember(projectId, userId);

        Process process = processRepository.findWeekMissionDetail(projectId, processId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));

        //  삭제 제외 + 정렬(공통)
        List<ProcessTaskItem> aliveItems = process.getTaskItems().stream()
                .filter(t -> t.getDeletedAt() == null)
                .sorted(Comparator.comparing(t -> t.getSortOrder() == null ? Integer.MAX_VALUE : t.getSortOrder()))
                .toList();

        //  task_items
        List<ProcessTaskItemResDto> taskItems = aliveItems.stream()
                .map(t -> new ProcessTaskItemResDto(
                        t.getId(),
                        t.getContent(),
                        t.isDone(),
                        t.getSortOrder(),
                        t.getDoneAt()
                ))
                .toList();

        // task_groups (리더형: roleField + customRoleFieldName 기준)
        record GroupKey(RoleField roleField, String customName) {}

        Map<GroupKey, List<ProcessTaskItem>> grouped = aliveItems.stream()
                .collect(Collectors.groupingBy(
                        t -> new GroupKey(t.getRoleField(), t.getCustomRoleFieldName()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<WeekMissionDetailResDto.TaskGroupResDto> taskGroups = grouped.entrySet().stream()
                .map(e -> {
                    GroupKey key = e.getKey();

                    List<ProcessTaskItemResDto> items = e.getValue().stream()
                            .sorted(Comparator.comparing(t -> t.getSortOrder() == null ? Integer.MAX_VALUE : t.getSortOrder()))
                            .map(t -> new ProcessTaskItemResDto(
                                    t.getId(),
                                    t.getContent(),
                                    t.isDone(),
                                    t.getSortOrder(),
                                    t.getDoneAt()
                            ))
                            .toList();

                    return new WeekMissionDetailResDto.TaskGroupResDto(
                            key.roleField(),
                            key.customName(),
                            items
                    );
                })
                // 그룹 순서 정렬
                .sorted((a, b) -> {
                    // null은 맨 뒤
                    int ra = (a.roleField() == null) ? Integer.MAX_VALUE : a.roleField().ordinal();
                    int rb = (b.roleField() == null) ? Integer.MAX_VALUE : b.roleField().ordinal();

                    // CUSTOM은 일반 RoleField 뒤로 보내고 싶으면 가중치
                    if (a.roleField() == RoleField.CUSTOM) ra += 1000;
                    if (b.roleField() == RoleField.CUSTOM) rb += 1000;

                    int cmp = Integer.compare(ra, rb);
                    if (cmp != 0) return cmp;

                    // 같은 roleField면 customFieldName 알파벳/가나다 순
                    String ca = (a.customFieldName() == null) ? "" : a.customFieldName();
                    String cb = (b.customFieldName() == null) ? "" : b.customFieldName();
                    return ca.compareTo(cb);
                })
                .toList();

        User leader = process.getCreatedBy();

        WeekMissionDetailResDto.AssigneeDto assignee = new WeekMissionDetailResDto.AssigneeDto(
                leader.getUserId(),
                leader.getName(),
                leader.getNickname(),
                leader.getProfileImageUrl()
        );

        // DTO 생성자 인자 순서 주의: (taskGroups, taskItems) 둘 다 넣기
        return new WeekMissionDetailResDto(
                process.getId(),
                process.getMissionNumber(),
                process.getTitle(),
                process.getContent(),
                process.getStatus(),
                process.getStartAt(),
                process.getEndAt(),
                assignee,
                taskGroups,
                taskItems,
                process.getCreatedAt(),
                process.getUpdatedAt()
        );
    }

    // 위크미션 TASK 프로세스 상태 변경 서비스
    @Transactional
    public void updateWeekMissionStatus(Long projectId, Long userId, Long processId, WeekMissionStatusUpdateReqDto req) {
        assertActiveLeader(projectId, userId);

        if (req == null || req.status() == null) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "status is required");
        }

        Process process = processRepository.findWeekMissionDetail(projectId, processId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.PROCESS_NOT_FOUND));


        ProcessStatus before = process.getStatus();
        ProcessStatus after = req.status();
        if(before == after) return;

        process.updateStatus(after);

        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.USER_NOT_FOUND, "userId=" + userId));

        Project project = process.getProject();

        notifyWorkspaceWeekMissionUpdated(project, actor, process);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processType", "WEEK_MISSION");
        meta.put("missionNumber", process.getMissionNumber());
        meta.put("title", process.getTitle());
        meta.put("beforeStatus", before.name());
        meta.put("afterStatus", after.name());

        publishWeekMissionHistory(
                projectId, userId, processId,
                HistoryAction.PROCESS_STATUS_CHANGED,
                meta
        );
    }

    // 위크미션 TASK 항목 수정
    @Transactional
    public ProcessTaskItemResDto updateWeekMissionTaskItem(
            Long projectId, Long userId, Long processId, Long taskItemId, WeekMissionTaskItemUpdateReqDto req
    ) {
        assertActiveLeader(projectId, userId);

        // 위크미션 존재 검증
        Process process = processRepository.findWeekMissionDetail(projectId, processId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.PROCESS_NOT_FOUND));

        ProcessTaskItem item = processTaskItemRepository.findByIdAndProcessIdAndDeletedAtIsNull(taskItemId, processId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.TASK_ITEM_NOT_FOUND));

        if (req == null) throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "request is null");

        boolean changed = false;

        // Before 스냅샷
        String beforeContent = item.getContent();
        Boolean beforeDone = item.isDone();
        Integer beforeSortOrder = item.getSortOrder();
        RoleField beforeRole = item.getRoleField();
        String beforeCustom = item.getCustomRoleFieldName();


        // content
        if (req.content() != null) {
            if (req.content().isBlank()) throw new ProcessException(ProcessErrorCode.INVALID_TASK_ITEM_CONTENT);
            String newContent = req.content().trim();
            if (!newContent.equals(beforeContent)) {
                item.updateContent(newContent);
                changed = true;
            }
        }

        // done
        if (req.isDone() != null) {
            boolean newDone = req.isDone();
            if (newDone != beforeDone) {
                item.updateDone(newDone);
                changed = true;
            }
        }


        // role 변경(원하면 허용 / 싫으면 이 블록 삭제)
        if (req.roleField() != null) {
            RoleField newRole = req.roleField();
            String newCustom = normalizeCustom(newRole, req.customRoleFieldName());

            boolean roleChanged =
                    newRole != beforeRole ||
                            (newRole == RoleField.CUSTOM && !java.util.Objects.equals(newCustom, beforeCustom));

            if (roleChanged) {
                // 이동 전 그룹 정보 저장
                RoleField oldRole = beforeRole;
                String oldCustom = beforeCustom;

                try {
                    item.updateRoleField(newRole, newCustom);
                } catch (IllegalArgumentException e) {
                    throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, e.getMessage());
                }

                // 이전 그룹 정규화
                normalizeGroupOrders(processId, oldRole, oldCustom);

                // 새 그룹 끝으로 보내고 정규화
                List<ProcessTaskItem> newGroup = processTaskItemRepository
                        .findWeekMissionGroupItemsOrdered(processId, newRole, newCustom);

                int nextOrder = newGroup.size() - 1;
                item.updateSortOrder(Math.max(nextOrder, 0));
                normalizeGroupOrders(processId, newRole, newCustom);

                changed = true;
            }
        }

        if (!changed) {
            return new ProcessTaskItemResDto(
                    item.getId(), item.getContent(), item.isDone(), item.getSortOrder(), item.getDoneAt()
            );
        }

        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.USER_NOT_FOUND, "userId=" + userId));
        Project project = process.getProject();

        notifyWorkspaceWeekMissionUpdated(project, actor, process);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("missionNumber", process.getMissionNumber());
        meta.put("title", process.getTitle());
        meta.put("taskItemId", item.getId());

        meta.put("before", Map.of(
                "content", beforeContent,
                "isDone", beforeDone,
                "sortOrder", beforeSortOrder,
                "roleField", beforeRole == null ? null : beforeRole.name(),
                "customRoleFieldName", beforeCustom
        ));
        meta.put("after", Map.of(
                "content", item.getContent(),
                "isDone", item.isDone(),
                "sortOrder", item.getSortOrder(),
                "roleField", item.getRoleField() == null ? null : item.getRoleField().name(),
                "customRoleFieldName", item.getCustomRoleFieldName()
        ));

        publishWeekMissionHistory(
                projectId, userId, processId,
                HistoryAction.TASK_ITEM_UPDATED,
                meta
        );



        return new ProcessTaskItemResDto(
                item.getId(),
                item.getContent(),
                item.isDone(),
                item.getSortOrder(),
                item.getDoneAt()
        );
    }

    private LocalDate toMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate resolveWeekStart(LocalDate requested, LocalDate fallbackBaseDate) {
        LocalDate base = (requested != null) ? requested : fallbackBaseDate;
        return toMonday(base);
    }
}