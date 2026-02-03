package com.nect.api.domain.team.process.service;

import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.*;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.enums.LaneType;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.process.*;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.enums.AssignmentRole;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.process.ProcessLaneOrderRepository;
import com.nect.core.repository.team.process.ProcessMentionRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessService {
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final ProcessMentionRepository processMentionRepository;
    private final UserRepository userRepository;
    private final ProcessLaneOrderRepository processLaneOrderRepository;

    private static final String TEAM_LANE_KEY = "TEAM";

    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_TASK_ITEMS = 30;


    // 제목 검증
    private void validateProcessTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "process_title is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_PROCESS_TITLE_LENGTH,
                    "process_title length=" + title.length() + ", max=" + MAX_TITLE_LENGTH
            );
        }
    }

    // taskItems 검증
    private void validateTaskItems(List<ProcessTaskItemReqDto> taskItems) {
        if (taskItems == null) return;

        if (taskItems.size() > MAX_TASK_ITEMS) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "task_items size=" + taskItems.size() + ", max=" + MAX_TASK_ITEMS
            );
        }

        for (int i = 0; i < taskItems.size(); i++) {
            ProcessTaskItemReqDto t = taskItems.get(i);
            if (t == null || t.content() == null || t.content().isBlank()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_TASK_ITEM_CONTENT,
                        "task_items[" + i + "].content is blank"
                );
            }
        }
    }

    private final NotificationFacade notificationFacade;
    private final ProjectHistoryPublisher historyPublisher;

    // 헬퍼 메서드
    private void assertActiveProjectMember(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ProcessException(
                    ProcessErrorCode.FORBIDDEN,
                    "프로젝트 멤버가 아닙니다. projectId=" + projectId + ", userId=" + userId
            );
        }
    }

    private String toDbLaneKey(String laneKey) {
        if (laneKey == null) return TEAM_LANE_KEY;
        String t = laneKey.trim();
        return t.isBlank() ? TEAM_LANE_KEY : t;
    }

    private String toApiLaneKey(String dbLaneKey) {
        if (dbLaneKey == null) return null;
        return TEAM_LANE_KEY.equals(dbLaneKey) ? null : dbLaneKey;
    }

    private void ensureLaneOrderRowsExist(Long projectId, ProcessStatus status, String dbLaneKey, List<Process> laneProcesses) {
        // 현재 laneProcesses에 포함된 애들에 대해 row가 없다면 tail로 생성
        long base = processLaneOrderRepository.countLaneTotal(projectId, dbLaneKey, status);
        int next = (int) base;

        for (Process p : laneProcesses) {
            var opt = processLaneOrderRepository.findByProjectIdAndProcessIdAndLaneKeyAndStatusAndDeletedAtIsNull(
                    projectId, p.getId(), dbLaneKey, status
            );
            if (opt.isPresent()) continue;

            ProcessLaneOrder row = ProcessLaneOrder.builder()
                    .projectId(projectId)
                    .process(p)
                    .laneKey(dbLaneKey)
                    .status(status)
                    .sortOrder(next++)
                    .build();

            processLaneOrderRepository.save(row);
        }
    }


    // 알림 관련 헬퍼 메서드
    private List<User> validateAndLoadMentionReceivers(Long projectId, Long actorId, List<Long> mentionIds) {
        if (mentionIds == null) return List.of();

        List<Long> ids = mentionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> !id.equals(actorId))
                .toList();

        if (ids.isEmpty()) return List.of();

        // 프로젝트 멤버인지 검증 + 유저 로드
        List<User> users = projectUserRepository.findAllUsersByProjectIdAndUserIds(projectId, ids);
        if (users.size() != ids.size()) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "mention_user_ids contains non-member user. projectId=" + projectId
            );
        }
        return users;
    }

    private void notifyWorkspaceMention(Project project, User actor, Long targetProcessId, List<User> receivers, String content) {
        if (receivers == null || receivers.isEmpty()) return;

        NotificationCommand command = new NotificationCommand(
                NotificationType.WORKSPACE_MENTIONED,
                NotificationClassification.WORK_STATUS,
                NotificationScope.WORKSPACE_GLOBAL,
                targetProcessId,
                new Object[]{ actor.getName() },
                new Object[]{ content },
                project
        );

        notificationFacade.notify(receivers, command);
    }

    // 프로세스 생성 서비스
    @Transactional
    public Long createProcess(Long projectId, Long userId, ProcessCreateReqDto req) {

        assertActiveProjectMember(projectId, userId);
        validateProcessTitle(req.processTitle());

        List<ProcessTaskItemReqDto> taskItems = req.taskItems();
        validateTaskItems(taskItems);

        // 프로젝트 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROJECT_NOT_FOUND,
                        "projectId = " + projectId
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.USER_NOT_FOUND,
                        "userId = " + userId
                ));

        // Process 기본 생성
        Process process = Process.builder()
                .project(project)
                .createdBy(user)
                .title(req.processTitle())
                .content(req.processContent())
                .build();

        // status 기본값 처리
        ProcessStatus status = (req.processStatus() == null) ? ProcessStatus.PLANNING : req.processStatus();
        process.updateStatus(status);

        // 기간 설정
        LocalDate start = req.startDate();
        LocalDate end = req.deadLine();

        if (start != null && end != null && start.isAfter(end)) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_PROCESS_PERIOD,
                    "startDate = " + start + ", deadLine = " + end
            );
        }

        process.updatePeriod(start, end);

        int i = 0;
        // 업무 리스트 저장
        for (var t : taskItems) {
            Integer order = (t.sortOrder() != null) ? t.sortOrder() : i++;
            ProcessTaskItem item = ProcessTaskItem.builder()
                    .process(process)
                    .content(t.content())
                    .isDone(Boolean.TRUE.equals(t.isDone()))
                    .sortOrder(order)
                    .build();

            process.addTaskItem(item);
        }

        // files
        List<Long> fileIds = Optional.ofNullable(req.fileIds()).orElse(List.of()).stream()
                .filter(Objects::nonNull).distinct().toList();

        for (Long fileId : fileIds) {
            SharedDocument doc = sharedDocumentRepository.findByIdAndProjectIdAndDeletedAtIsNull(fileId, projectId)
                    .orElseThrow(() -> new ProcessException(
                            ProcessErrorCode.SHARED_DOCUMENT_NOT_FOUND,
                            "projectId=" + projectId + ", fileId=" + fileId
                    ));
            process.attachDocument(doc);
        }

        // links
        List<String> links = Optional.ofNullable(req.links()).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        for (String url : links) {
            Link link = Link.builder()
                    .process(process)
                    .url(url)
                    .build();
            process.addLink(link);
        }


        // 필드(역할) CUSTOM쪽 검증
        List<RoleField> roleFields = Optional.ofNullable(req.roleFields()).orElse(List.of())
                .stream().filter(Objects::nonNull).distinct().toList();


        if (roleFields.contains(RoleField.CUSTOM)) {
            if (req.customFieldName() == null || req.customFieldName().isBlank()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "custom_field_name is required when role_fields contains CUSTOM"
                );
            }
        }

        for (RoleField rf : roleFields) {
            if (rf == RoleField.CUSTOM) {
                process.addField(RoleField.CUSTOM, req.customFieldName());
            } else {
                process.addField(rf, null);
            }
        }


        List<Long> assigneeIds = Optional.ofNullable(req.assigneeIds()).orElse(List.of()).stream()
                .filter(Objects::nonNull).distinct().toList();

        if (!assigneeIds.isEmpty()) {
            List<User> assignees = projectUserRepository.findAllUsersByProjectIdAndUserIds(projectId, assigneeIds);

            if (assignees.size() != assigneeIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "assignee must be active project member. projectId=" + projectId
                );
            }

            Map<Long, User> byId = assignees.stream()
                    .collect(Collectors.toMap(User::getUserId, u -> u));

            for (Long aid : assigneeIds) {
                User assignee = byId.get(aid);

                ProcessUser pu = ProcessUser.builder()
                        .process(process)
                        .user(assignee)
                        .assignmentRole(AssignmentRole.ASSIGNEE)
                        .build();

                process.addProcessUser(pu);
            }
        }

        Process saved = processRepository.save(process);

        List<Long> mentionIds = Optional.ofNullable(req.mentionUserIds())
                .orElse(List.of())
                .stream().filter(Objects::nonNull).distinct().toList();

        List<User> mentionReceivers = validateAndLoadMentionReceivers(projectId, userId, mentionIds);

        syncMentions(saved, mentionIds);

        // 멘션된 사람들에게 알림
        notifyWorkspaceMention(
                project,
                user,
                saved.getId(),
                mentionReceivers,
                saved.getTitle()
        );


        /*
         * HISTORY: Process 생성 완료 후 이벤트 발행
         * - 저장은 HistoryEventHandler가 AFTER_COMMIT 시점에 수행(트랜잭션 성공 시에만 기록)
         * - metaJson에는 생성 시점 핵심 스냅샷(title/status/period)만 담는다.
         * */
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", saved.getTitle());
        meta.put("status", saved.getStatus());
        meta.put("startAt", saved.getStartAt());
        meta.put("endAt", saved.getEndAt());
        meta.put("roleFields", roleFields);
        meta.put("customFieldName", roleFields.contains(RoleField.CUSTOM) ? req.customFieldName() : null);
        meta.put("assigneeIds", assigneeIds);
        meta.put("mentionUserIds", mentionIds);
        meta.put("fileIds", fileIds);
        meta.put("links", links);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.PROCESS_CREATED,
                HistoryTargetType.PROCESS,
                saved.getId(),
                meta
        );

        return saved.getId();
    }


    // 프로세스 상세 보기
    @Transactional(readOnly = true)
    public ProcessDetailResDto getProcessDetail(Long projectId, Long userId, Long processId, String laneKey) {
        assertActiveProjectMember(projectId, userId);

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));

        String dbLaneKey = toDbLaneKey(laneKey);

        // lane 기준 정렬값(status_order) 조회
        // - 없으면 null
        Integer laneStatusOrder = processLaneOrderRepository
                .findByProjectIdAndProcessIdAndLaneKeyAndStatusAndDeletedAtIsNull(
                        projectId,
                        processId,
                        dbLaneKey,
                        process.getStatus()
                )
                .map(ProcessLaneOrder::getSortOrder)
                .orElse(null);

        List<ProcessTaskItemResDto> taskItems = process.getTaskItems().stream()
                .sorted(Comparator.comparing(t -> t.getSortOrder() == null ? Integer.MAX_VALUE : t.getSortOrder()))
                .map(t -> new ProcessTaskItemResDto(
                        t.getId(),
                        t.getContent(),
                        t.isDone(),
                        t.getSortOrder(),
                        t.getDoneAt()
                ))
                .toList();


        List<LinkResDto> links = process.getLinks().stream()
                .map(l -> new LinkResDto(l.getId(), l.getUrl()))
                .toList();

        List<Long> mentionUserIds = process.getMentions().stream()
                .map(ProcessMention::getMentionedUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<RoleField> roleFields = process.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .map(ProcessField::getRoleField)
                .filter(Objects::nonNull)
                .filter(rf -> rf != RoleField.CUSTOM)
                .distinct()
                .toList();

        List<String> customFields = process.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                .map(ProcessField::getCustomFieldName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        List<AssigneeResDto> assignees = process.getProcessUsers().stream()
                .filter(pu -> pu.getDeletedAt() == null)
                .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                .map(pu -> {
                    User u = pu.getUser();

                    // TODO : 유저 프로필 넣기
                    String userImage = null;

                    return new AssigneeResDto(
                            u.getUserId(),
                            u.getName(),
                            userImage
                    );
                })
                .toList();

        List<FileResDto> files = process.getSharedDocuments().stream()
                .filter(psd -> psd.getDeletedAt() == null)
                .map(ProcessSharedDocument::getDocument)
                .filter(Objects::nonNull)
                .map(doc -> new FileResDto(
                        doc.getId(),
                        doc.getFileName(),
                        doc.getFileUrl(),
                        doc.getFileExt(),
                        doc.getFileSize()
                ))
                .toList();

        // feedbacks 채우기
        List<Long> feedbackCreatedByUserIds = process.getFeedbacks().stream()
                .filter(f -> f.getDeletedAt() == null)
                .map(f -> f.getCreatedBy() == null ? null : f.getCreatedBy().getUserId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, List<String>> createdByRoleFieldLabelsMap =
                projectUserRepository
                        .findActiveUserRoleFieldsByProjectIdAndUserIds(projectId, feedbackCreatedByUserIds)
                        .stream()
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

        // feedbacks 채우기
        List<ProcessFeedbackCreateResDto> feedbacks = process.getFeedbacks().stream()
                .filter(f -> f.getDeletedAt() == null)
                .map(f -> {
                    User createdBy = f.getCreatedBy();

                    Long createdById = (createdBy == null) ? null : createdBy.getUserId();
                    List<String> createdByRoleFieldLabels = (createdById == null)
                            ? List.of()
                            : createdByRoleFieldLabelsMap.getOrDefault(createdById, List.of());

                    FeedbackCreatedByResDto createdByRes = new FeedbackCreatedByResDto(
                            createdById,
                            createdBy == null ? null : createdBy.getName(),
                            createdByRoleFieldLabels
                    );

                    return new ProcessFeedbackCreateResDto(
                            f.getId(),
                            f.getContent(),
                            f.getStatus(),
                            createdByRes,
                            f.getCreatedAt()
                    );
                })
                .toList();

        return new ProcessDetailResDto(
                process.getId(),
                process.getTitle(),
                process.getContent(),
                process.getStatus(),
                process.getStartAt(),
                process.getEndAt(),
                laneStatusOrder,
                roleFields,
                customFields,
                assignees,
                mentionUserIds,
                files,
                links,
                taskItems,
                feedbacks,
                process.getCreatedAt(),
                process.getUpdatedAt(),
                process.getDeletedAt()
        );
    }

    private List<Long> syncMentions(Process process, List<Long> requestedUserIds) {
        // null이면 언급 변경 안 함
        if (requestedUserIds == null) return null;

        // 빈 리스트면 언급 전부 제거 의미로 해석
        List<Long> req = requestedUserIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 삭제 포함 전체 조회
        List<ProcessMention> all = processMentionRepository.findAllByProcessId(process.getId());

        Map<Long, ProcessMention> byUserId = new HashMap<>();
        for (ProcessMention m : all) {
            byUserId.put(m.getMentionedUserId(), m);
        }

        // 요청에 없는 기존 mention -> soft delete
        for (ProcessMention m : all) {
            if (!req.contains(m.getMentionedUserId())) {
                m.softDelete();
            }
        }

        // 요청에 있는 mention -> restore or create
        for (Long uid : req) {
            ProcessMention existing = byUserId.get(uid);
            if (existing != null) {
                if (existing.isDeleted()) existing.restore();
            } else {
                ProcessMention created = ProcessMention.builder()
                        .process(process)
                        .mentionedUserId(uid)
                        .build();
                processMentionRepository.save(created);
            }
        }

        return req;
    }

    // 프로세스 기본 정보 수정
    @Transactional
    public ProcessBasicUpdateResDto updateProcessBasic(Long projectId, Long userId, Long processId, ProcessBasicUpdateReqDto req) {
        assertActiveProjectMember(projectId, userId);

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));


        // before 스냅샷
        final String beforeTitle = process.getTitle();
        final String beforeContent = process.getContent();
        final ProcessStatus beforeStatus = process.getStatus();
        final LocalDate beforeStart = process.getStartAt();
        final LocalDate beforeEnd = process.getEndAt();

        final List<Long> beforeMentionIds = processMentionRepository.findAllByProcessId(process.getId()).stream()
                .filter(m -> !m.isDeleted())
                .map(ProcessMention::getMentionedUserId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        final List<RoleField> beforeRoleFields = process.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .map(ProcessField::getRoleField)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        final List<String> beforeCustomFields = process.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                .map(ProcessField::getCustomFieldName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted()
                .toList();

        final List<Long> beforeAssigneeIds = process.getProcessUsers().stream()
                .filter(pu -> pu.getDeletedAt() == null)
                .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                .map(pu -> pu.getUser().getUserId())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        if(req.processTitle() != null && !req.processTitle().isBlank()) {
            process.updateTitle(req.processTitle());
        }

        if(req.processContent() != null){
            process.updateContent(req.processContent());
        }

        if(req.processStatus() != null){
            process.updateStatus(req.processStatus());
        }


        // 기간 검증
        // - start만 오면 start만 변경, end는 유지
        // - end만 오면 end만 변경, start는 유지
        // - 둘 다 오면 검증 후 둘 다 변경
        LocalDate newStart = req.startDate();
        LocalDate newEnd = req.deadLine();

        LocalDate mergedStart = (newStart != null) ? newStart : process.getStartAt();
        LocalDate mergedEnd = (newEnd != null) ? newEnd : process.getEndAt();

        if (mergedStart != null && mergedEnd != null && mergedStart.isAfter(mergedEnd)) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_PROCESS_PERIOD,
                    "startDate = " + mergedStart + ", deadLine = " + mergedEnd
            );
        }

        if (newStart != null || newEnd != null) {
            process.updatePeriod(mergedStart, mergedEnd);
        }


        // 멘션 교체
        List<Long> mentionIdsForRes = syncMentions(process, req.mentionUserIds());

        // 멘션 알림: 요청이 들어온 경우에만, 그리고 '새로 추가된 멘션'에게만 전송
        if (req.mentionUserIds() != null) {
            List<Long> afterIds = (mentionIdsForRes == null) ? List.of() : mentionIdsForRes.stream()
                    .filter(Objects::nonNull).distinct().toList();

            Set<Long> beforeSet = new HashSet<>(beforeMentionIds);
            List<Long> addedMentionIds = afterIds.stream()
                    .filter(id -> !beforeSet.contains(id))
                    .toList();

            if (!addedMentionIds.isEmpty()) {
                User actor = userRepository.findById(userId)
                        .orElseThrow(() -> new ProcessException(ProcessErrorCode.INVALID_REQUEST, "userId=" + userId));

                List<User> receivers = validateAndLoadMentionReceivers(projectId, userId, addedMentionIds);

                notifyWorkspaceMention(
                        process.getProject(),
                        actor,
                        process.getId(),
                        receivers,
                        process.getTitle()
                );
            }
        }

        if (req.roleFields() != null || req.customFields() != null) {
            //  기존 전부 soft delete
            process.getProcessFields().forEach(pf -> {
                if (pf.getDeletedAt() == null) pf.softDelete();
            });

            // roleFields 반영 (CUSTOM 제외)
            List<RoleField> requestedRoleFields = (req.roleFields() == null) ? List.of() : req.roleFields();
            for (RoleField rf : requestedRoleFields) {
                if (rf == null) continue;
                if (rf == RoleField.CUSTOM) continue;

                // 기존에 같은 roleField가 삭제된 상태로 있으면 restore, 없으면 생성
                ProcessField found = process.getProcessFields().stream()
                        .filter(pf -> pf.getRoleField() == rf)
                        .findFirst()
                        .orElse(null);

                if (found != null) {
                    found.restore();
                } else {
                    ProcessField pf = ProcessField.builder()
                            .process(process)
                            .roleField(rf)
                            .customFieldName(null)
                            .build();
                    process.getProcessFields().add(pf);
                }
            }

            // customFields 반영 (CUSTOM은 이름 기반)
            List<String> requestedCustomFields = (req.customFields() == null) ? List.of() : req.customFields();
            for (String name : requestedCustomFields) {
                if (name == null) continue;
                String trimmed = name.trim();
                if (trimmed.isBlank()) continue;

                ProcessField found = process.getProcessFields().stream()
                        .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                        .filter(pf -> trimmed.equals(pf.getCustomFieldName()))
                        .findFirst()
                        .orElse(null);

                if (found != null) {
                    found.restore();
                } else {
                    ProcessField pf = ProcessField.builder()
                            .process(process)
                            .roleField(RoleField.CUSTOM)
                            .customFieldName(trimmed)
                            .build();
                    process.getProcessFields().add(pf);
                }
            }
        }

        // 요청이 null이면 변경 안 함
        if (req.assigneeIds() != null) {
            List<Long> requestedAssigneeIds = req.assigneeIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            // 프로젝트 멤버인지 검증
            List<User> assigneeUsers = projectUserRepository.findAllUsersByProjectIdAndUserIds(projectId, requestedAssigneeIds);
            if (assigneeUsers.size() != requestedAssigneeIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "assignee_ids contains non-member user. projectId=" + projectId
                );
            }

            // 기존 ASSIGNEE 전부 삭제
            process.getProcessUsers().stream()
                    .filter(pu -> pu.getDeletedAt() == null)
                    .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                    .forEach(ProcessUser::delete);

            // 요청 assignee 반영: restore or create
            for (User u : assigneeUsers) {
                ProcessUser found = process.getProcessUsers().stream()
                        .filter(pu -> pu.getUser() != null && pu.getUser().getUserId().equals(u.getUserId()))
                        .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                        .findFirst()
                        .orElse(null);

                if (found != null) {
                    found.restore();
                } else {
                    ProcessUser pu = ProcessUser.builder()
                            .process(process)
                            .user(u)
                            .assignmentRole(AssignmentRole.ASSIGNEE)
                            .assignedAt(null)
                            .build();
                    process.getProcessUsers().add(pu);
                }
            }
        }

        // after 스냅샷
        final String afterTitle = process.getTitle();
        final String afterContent = process.getContent();
        final ProcessStatus afterStatus = process.getStatus();
        final LocalDate afterStart = process.getStartAt();
        final LocalDate afterEnd = process.getEndAt();

        final List<Long> afterMentionIds = (mentionIdsForRes == null)
                ? null   // 요청이 null이면 멘션 변경 안함
                : mentionIdsForRes.stream().filter(Objects::nonNull).distinct().sorted().toList();

        final List<RoleField> afterRoleFields = process.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .map(ProcessField::getRoleField)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        final List<String> afterCustomFields = process.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                .map(ProcessField::getCustomFieldName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted()
                .toList();

        final List<Long> afterAssigneeIds = process.getProcessUsers().stream()
                .filter(pu -> pu.getDeletedAt() == null)
                .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                .map(pu -> pu.getUser().getUserId())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        // Map 값 변경
        Map<String, Object> changed = new LinkedHashMap<>();

        if (!Objects.equals(beforeTitle, afterTitle))
            changed.put("title", Map.of("before", beforeTitle, "after", afterTitle));

        if (!Objects.equals(beforeContent, afterContent))
            changed.put("content", Map.of("before", beforeContent, "after", afterContent));

        if (beforeStatus != afterStatus)
            changed.put("status", Map.of("before", beforeStatus, "after", afterStatus));

        if (!Objects.equals(beforeStart, afterStart) || !Objects.equals(beforeEnd, afterEnd)) {
            changed.put("period", Map.of(
                    "before", Map.of("startAt", beforeStart, "endAt", beforeEnd),
                    "after", Map.of("startAt", afterStart, "endAt", afterEnd)
            ));
        }

        // 멘션 요청이 들어온 경우에만 변경 여부 판단
        if (afterMentionIds != null && !Objects.equals(beforeMentionIds, afterMentionIds)) {
            changed.put("mentions", Map.of("before", beforeMentionIds, "after", afterMentionIds));
        }

        // 요청이 들어온 경우에만 비교 (PATCH)
        if ((req.roleFields() != null || req.customFields() != null) &&
                (!Objects.equals(beforeRoleFields, afterRoleFields) || !Objects.equals(beforeCustomFields, afterCustomFields))) {
            changed.put("fields", Map.of(
                    "before", Map.of("roleFields", beforeRoleFields, "customFields", beforeCustomFields),
                    "after", Map.of("roleFields", afterRoleFields, "customFields", afterCustomFields)
            ));
        }

        if (req.assigneeIds() != null && !Objects.equals(beforeAssigneeIds, afterAssigneeIds)) {
            changed.put("assignees", Map.of("before", beforeAssigneeIds, "after", afterAssigneeIds));
        }

        /*
         * HISTORY: 실제 변경이 있는 경우에만 PROCESS_UPDATED 이벤트 발행
         * - 발행 조건: changed(변경된 필드 목록)가 비어있지 않을 때
         * - metaJson: changed 요약 + before/after 스냅샷(디버깅/감사 추적용)
         * - 저장은 HistoryEventHandler가 AFTER_COMMIT 시점에 수행(트랜잭션 성공 시에만 기록)
         * */
        if (!changed.isEmpty()) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("changed", changed);
            meta.put("before", Map.of(
                    "title", beforeTitle,
                    "content", beforeContent,
                    "status", beforeStatus,
                    "startAt", beforeStart,
                    "endAt", beforeEnd,
                    "mentionUserIds", beforeMentionIds
            ));
            meta.put("after", Map.of(
                    "title", afterTitle,
                    "content", afterContent,
                    "status", afterStatus,
                    "startAt", afterStart,
                    "endAt", afterEnd,
                    "mentionUserIds", (afterMentionIds != null ? afterMentionIds : beforeMentionIds)
            ));

            historyPublisher.publish(
                    projectId,
                    userId,
                    HistoryAction.PROCESS_UPDATED,
                    HistoryTargetType.PROCESS,
                    process.getId(),
                    meta
            );
        }

        return new ProcessBasicUpdateResDto(
                process.getId(),
                process.getTitle(),
                process.getContent(),
                process.getStatus(),
                process.getStartAt(),
                process.getEndAt(),
                afterRoleFields,
                afterCustomFields,
                afterAssigneeIds,
                (afterMentionIds == null) ? beforeMentionIds : afterMentionIds,
                process.getUpdatedAt()
        );

    }




    /** 프로세스 삭제
     * 프로세스를 soft delete 처리하며, 정책에 따라 하위 엔티티(업무/피드백/첨부/링크)에도 삭제 상태를 전파한다.
     *
     * @param projectId 프로젝트 ID
     * @param processId 프로세스 ID
     */
    @Transactional
    public void deleteProcess(Long projectId, Long userId, Long processId) {
        assertActiveProjectMember(projectId, userId);

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "processId=" + processId + ", projectId=" + projectId
                ));

        final String beforeTitle = process.getTitle();
        final ProcessStatus beforeStatus = process.getStatus();

        process.softDeleteCascade();

        processMentionRepository.softDeleteAllByProcessId(process.getId(), java.time.LocalDateTime.now());

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", beforeTitle);
        meta.put("status", beforeStatus);
        meta.put("deletedAt", process.getDeletedAt());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.PROCESS_DELETED,
                HistoryTargetType.PROCESS,
                process.getId(),
                meta
        );
    }



    private LocalDate normalizeWeekStart(LocalDate startDate) {
        LocalDate base = (startDate == null) ? LocalDate.now() : startDate;
        return base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private ProcessCardResDto toProcessCardResDTO(Process p) {
        int whole = (p.getTaskItems() == null) ? 0 : p.getTaskItems().size();
        int done = (p.getTaskItems() == null) ? 0 : (int) p.getTaskItems().stream()
                .filter(ProcessTaskItem::isDone)
                .count();

        Integer leftDay = calcLeftDay(p.getEndAt());

        List<RoleField> roleFields = p.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .map(ProcessField::getRoleField)
                .filter(Objects::nonNull)
                .filter(rf -> rf != RoleField.CUSTOM) // 커스텀은 별도 리스트로
                .distinct()
                .toList();

        List<String> customFields = p.getProcessFields().stream()
                .filter(pf -> pf.getDeletedAt() == null)
                .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                .map(ProcessField::getCustomFieldName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        List<AssigneeResDto> assignees = p.getProcessUsers().stream()
                .filter(pu -> pu.getDeletedAt() == null)
                .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                .map(pu -> {
                    User u = pu.getUser();
                    String userImage = null; // TODO: 프로필 컬럼/연동되면 세팅
                    return new AssigneeResDto(u.getUserId(), u.getName(), userImage);
                })
                .toList();


        return new ProcessCardResDto(
                p.getId(),
                p.getStatus(),
                p.getTitle(),
                done,
                whole,
                p.getStartAt(),
                p.getEndAt(),
                leftDay,
                roleFields,
                customFields,
                assignees
        );
    }

    private Integer calcLeftDay(LocalDate deadLine) {
        if (deadLine == null) return null;
        long diff = ChronoUnit.DAYS.between(LocalDate.now(), deadLine);
        return (int) Math.max(diff, 0);
    }

    // 주 DTO 만들기 레인 분리 포함
    private ProcessWeekResDto buildWeekDto(LocalDate weekStart, List<ProcessCardResDto> weekCards) {

        // 공통 레인: roleFields/customFields 모두 비어있으면 Team
        List<ProcessCardResDto> commonLane = weekCards.stream()
                .filter(c ->
                        (c.roleFields() == null || c.roleFields().isEmpty()) &&
                                (c.customFields() == null || c.customFields().isEmpty())
                )
                .toList();

        Map<String, List<ProcessCardResDto>> byLaneMap = new LinkedHashMap<>();

        for (ProcessCardResDto c : weekCards) {

            // 카드가 같은 lane에 중복으로 들어가는 걸 방지
            java.util.Set<String> laneKeys = new java.util.LinkedHashSet<>();

            // RoleField 레인 (CUSTOM은 customFields로만 처리)
            if (c.roleFields() != null) {
                for (RoleField rf : c.roleFields()) {
                    if (rf == null) continue;
                    if (rf == RoleField.CUSTOM) continue;
                    laneKeys.add("ROLE:" + rf.name());
                }
            }

            // Custom 레인
            if (c.customFields() != null) {
                for (String name : c.customFields()) {
                    if (name == null) continue;
                    String trimmed = name.trim();
                    if (trimmed.isBlank()) continue;
                    laneKeys.add("CUSTOM:" + trimmed);
                }
            }

            for (String key : laneKeys) {
                byLaneMap.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
            }
        }

        // laneKey 기준으로 결정론적 정렬: ROLE 먼저, 그 다음 CUSTOM
        List<FieldGroupResDto> byField = byLaneMap.entrySet().stream()
                .map(e -> {
                    String laneKey = e.getKey();

                    String laneName;
                    int laneTypeOrder; // ROLE=0, CUSTOM=1
                    String laneSortKey; // 타입 내부 정렬 키

                    if (laneKey.startsWith("ROLE:")) {
                        laneTypeOrder = 0;
                        laneName = laneKey.substring("ROLE:".length());   // enum name
                        laneSortKey = laneName; // enum name 기준 오름차순
                    } else {
                        laneTypeOrder = 1;
                        laneName = laneKey.substring("CUSTOM:".length()); // custom name
                        laneSortKey = laneName; // custom name 기준 오름차순
                    }

                    // FieldGroupResDto가 lane DTO 역할이면 fieldOrder를 "정렬용 order"로 쓰면 됨
                    // (혹은 fieldOrder 필드를 laneTypeOrder로 두고, laneName으로 2차 정렬)
                    return new FieldGroupResDto(
                            laneKey,
                            laneName,
                            laneTypeOrder,
                            e.getValue()
                    );
                })
                .sorted(java.util.Comparator
                        .comparingInt(FieldGroupResDto::fieldOrder)   // ROLE(0) 먼저
                        .thenComparing(FieldGroupResDto::fieldName))  // 이름 오름차순
                .toList();

        return new ProcessWeekResDto(weekStart, commonLane, byField);
    }

    // 주차별 프로세스 조회 서비스
    @Transactional(readOnly = true)
    public ProcessWeekResDto getWeekProcesses(Long projectId, Long userId, LocalDate startDate) {

        assertActiveProjectMember(projectId, userId);

        if (!projectRepository.existsById(projectId)) {
            throw new ProcessException(
                    ProcessErrorCode.PROJECT_NOT_FOUND,
                    "projectId=" + projectId
            );
        }


        LocalDate weekStart = normalizeWeekStart(startDate);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<Process> processes = processRepository.findAllInRangeOrdered(projectId, weekStart, weekEnd);

        List<ProcessCardResDto> cards = processes.stream()
                .map(p -> {
                    int whole = (p.getTaskItems() == null) ? 0 : p.getTaskItems().size();
                    int done = (p.getTaskItems() == null) ? 0 : (int) p.getTaskItems().stream()
                            .filter(ProcessTaskItem::isDone)
                            .count();

                    Integer leftDay = null;
                    if (p.getEndAt() != null) {
                        long diff = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), p.getEndAt());
                        leftDay = (int) Math.max(diff, 0);
                    }

                    // roleFields / customFields
                    List<RoleField> roleFields = p.getProcessFields().stream()
                            .filter(pf -> pf.getDeletedAt() == null)
                            .map(ProcessField::getRoleField)
                            .filter(java.util.Objects::nonNull)
                            .filter(rf -> rf != RoleField.CUSTOM)   // CUSTOM은 customFields로만
                            .distinct()
                            .toList();

                    List<String> customFields = p.getProcessFields().stream()
                            .filter(pf -> pf.getDeletedAt() == null)
                            .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                            .map(ProcessField::getCustomFieldName)
                            .filter(java.util.Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .distinct()
                            .toList();

                    // assignees
                    List<AssigneeResDto> assignees = p.getProcessUsers().stream()
                            .filter(pu -> pu.getDeletedAt() == null)
                            .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                            .map(pu -> {
                                User u = pu.getUser();
                                String userImage = null; // TODO: 프로필 이미지 연결되면 세팅
                                return new AssigneeResDto(u.getUserId(), u.getName(), userImage);
                            })
                            .toList();

                    return new ProcessCardResDto(
                            p.getId(),
                            p.getStatus(),
                            p.getTitle(),
                            done,
                            whole,
                            p.getStartAt(),
                            p.getEndAt(),
                            leftDay,
                            roleFields,
                            customFields,
                            assignees
                    );
                })
                .toList();

        return buildWeekDto(weekStart, cards);

    }



    private boolean isRoleLane(String laneKey) {
        return laneKey != null && laneKey.startsWith("ROLE:");
    }

    private boolean isCustomLane(String laneKey) {
        return laneKey != null && laneKey.startsWith("CUSTOM:");
    }

    private RoleField parseRoleField(String laneKey) {
        // laneKey = "ROLE:BACKEND"
        try {
            String raw = laneKey.substring("ROLE:".length()).trim();
            return RoleField.valueOf(raw);
        } catch (Exception e) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "invalid lane_key(role). laneKey=" + laneKey
            );
        }
    }

    private String parseCustomName(String laneKey) {
        // laneKey = "CUSTOM:영상편집"
        String name = laneKey.substring("CUSTOM:".length()).trim();
        if (name.isBlank()) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "invalid lane_key(custom). laneKey=" + laneKey
            );
        }
        return name;
    }

    // 파트별 프로세스 조회 서비스
    @Transactional(readOnly = true)
    public ProcessPartResDto getPartProcesses(Long projectId, Long userId, String laneKey) {

        assertActiveProjectMember(projectId, userId);

        if (!projectRepository.existsById(projectId)) {
            throw new ProcessException(
                    ProcessErrorCode.PROJECT_NOT_FOUND,
                    "projectId=" + projectId
            );
        }

        // DB laneKey로 변환 (Team이면 TEAM)
        String dbLaneKey = toDbLaneKey(laneKey);

        /**
         * laneKey 정책
         * - null or blank => Team 탭(전체)
         * - "ROLE:XXX"    => RoleField 기반 필터
         * - "CUSTOM:이름"  => customFieldName 기반 필터
         */

        // lane 대상 프로세스 목록 (멤버십/필터 검증 + row 생성용)
        List<Process> laneProcesses = List.of();

        if (TEAM_LANE_KEY.equals(dbLaneKey)) { // 팀(전체)
            laneProcesses = processRepository.findAllForTeamBoard(projectId);
        }else if (isRoleLane(dbLaneKey)) { // 역할
            RoleField roleField = parseRoleField(dbLaneKey);
            if (roleField == RoleField.CUSTOM) {
                throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "ROLE lane cannot be CUSTOM. laneKey=" + laneKey);
            }
            laneProcesses = processRepository.findAllForRoleLaneBoard(projectId, roleField);
        }else if (isCustomLane(dbLaneKey)) { // CUSTOM(직접 입력)
            String customName = parseCustomName(dbLaneKey);
            laneProcesses = processRepository.findAllForCustomLaneBoard(projectId, customName);
        } else {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "invalid lane_key prefix. laneKey=" + laneKey);
        }


        List<ProcessStatusGroupResDto> groups = List.of(
                buildStatusGroupOrdered(projectId, dbLaneKey, ProcessStatus.PLANNING, laneProcesses),
                buildStatusGroupOrdered(projectId, dbLaneKey, ProcessStatus.IN_PROGRESS, laneProcesses),
                buildStatusGroupOrdered(projectId, dbLaneKey, ProcessStatus.DONE, laneProcesses),
                buildStatusGroupOrdered(projectId, dbLaneKey, ProcessStatus.BACKLOG, laneProcesses)
        );

        return new ProcessPartResDto(toApiLaneKey(dbLaneKey), groups);
    }

    private ProcessStatusGroupResDto buildStatusGroupOrdered(
            Long projectId,
            String laneKey,
            ProcessStatus status,
            List<Process> laneProcessesAll
    ) {
        // 해당 status인 프로세스만
        List<Process> laneProcesses = laneProcessesAll.stream()
                .filter(p -> p.getStatus() == status)
                .toList();

        // order row 없으면 생성 (tail 부여)
        ensureLaneOrderRowsExist(projectId, status, laneKey, laneProcesses);

        // order row 기준 processId 순서 확보
        List<ProcessLaneOrder> orders = processLaneOrderRepository.findLaneOrders(projectId, laneKey, status);
        List<Long> orderedIds = orders.stream().map(o -> o.getProcess().getId()).toList();

        // 혹시라도 (order에는 있는데 laneProcesses에는 없는) 케이스 방어
        // laneProcesses 기준으로 map
        Map<Long, Process> map = laneProcesses.stream().collect(Collectors.toMap(Process::getId, p -> p));

        List<ProcessCardResDto> cards = new ArrayList<>();
        for (Long id : orderedIds) {
            Process p = map.get(id);
            if (p != null) cards.add(toProcessCardResDTO(p));
        }

        return new ProcessStatusGroupResDto(status, cards.size(), cards);
    }

    // 파트별 작업 진행률 조회 서비스
    @Transactional(readOnly = true)
    public ProcessProgressSummaryResDto getPartProgressSummary(Long projectId, Long userId) {
        assertActiveProjectMember(projectId, userId);

        if (!projectRepository.existsById(projectId)) {
            throw new ProcessException(ProcessErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId);
        }

        List<ProcessStatus> statuses = List.of(
                ProcessStatus.PLANNING,
                ProcessStatus.IN_PROGRESS,
                ProcessStatus.DONE
        );

        RoleField custom = RoleField.CUSTOM;

        List<ProcessRepository.LaneStatusCountRow> roleRows =
                processRepository.countRoleLaneStatusForProgressSummary(projectId, custom, statuses);

        List<ProcessRepository.LaneStatusCountRow> customRows =
                processRepository.countCustomLaneStatusForProgressSummary(projectId, custom, statuses);

        // laneKey -> status -> count
        Map<String, EnumMap<ProcessStatus, Long>> laneCounts = new LinkedHashMap<>();

        // ROLE lanes
        for (var r : roleRows) {
            RoleField rf = r.getRoleField();
            if (rf == null) continue;

            String laneKey = "ROLE:" + rf.name();
            laneCounts.computeIfAbsent(laneKey, k -> new EnumMap<>(ProcessStatus.class))
                    .put(r.getStatus(), r.getCnt());
        }

        // CUSTOM lanes
        for (var r : customRows) {
            String name = r.getCustomName();
            if (name == null) continue;

            String trimmed = name.trim();
            if (trimmed.isBlank()) continue;

            String laneKey = "CUSTOM:" + trimmed;
            laneCounts.computeIfAbsent(laneKey, k -> new EnumMap<>(ProcessStatus.class))
                    .put(r.getStatus(), r.getCnt());
        }


        // 정렬 : ROLE 먼저, CUSTOM 다음, 이름 오름차순으로
        List<String> sortedKeys = laneCounts.keySet().stream()
                .sorted((a, b) -> {
                    int ta = a.startsWith("ROLE:") ? 0 : 1;
                    int tb = b.startsWith("CUSTOM:") ? 0 : 1;
                    if(ta != tb) return Integer.compare(ta, tb);
                    return a.compareTo(b);
                })
                .toList();

        List<LaneProgressResDto> lanes = sortedKeys.stream()
                .map(laneKey -> {
                    EnumMap<ProcessStatus, Long> m = laneCounts.get(laneKey);

                    long planning = m.getOrDefault(ProcessStatus.PLANNING, 0L);
                    long inProgress = m.getOrDefault(ProcessStatus.IN_PROGRESS, 0L);
                    long done = m.getOrDefault(ProcessStatus.DONE, 0L);
                    long total = planning + inProgress + done;

                    int planningRate = rate(planning, total);
                    int inProgressRate = rate(inProgress, total);
                    int doneRate = (total == 0) ? 0 : Math.max(0, 100 - planningRate - inProgressRate);

                    LaneType laneType = laneKey.startsWith("ROLE:") ? LaneType.ROLE : LaneType.CUSTOM;
                    String laneName = laneType == LaneType.ROLE
                            ? laneKey.substring("ROLE:".length())
                            : laneKey.substring("CUSTOM:".length());

                    return new LaneProgressResDto(
                            laneKey,
                            laneType,
                            laneName,
                            planning,
                            inProgress,
                            done,
                            total,
                            planningRate,
                            inProgressRate,
                            doneRate
                    );
                })
                .toList();

        return new ProcessProgressSummaryResDto(lanes);
    }

    private int rate(long part, long total) {
        if (total == 0) return 0;
        return (int) Math.round(part * 100.0 / total);
    }


    // 프로세스 위치 상태 정렬 변경 서비스
    @Transactional
    public ProcessOrderUpdateResDto updateProcessOrder(Long projectId, Long userId, Long processId, ProcessOrderUpdateReqDto req) {
        assertActiveProjectMember(projectId, userId);

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));

        // laneKey (Team이면 TEAM)
        String dbLaneKey = toDbLaneKey(req.laneKey());
        String apiLaneKeyForHistory = toApiLaneKey(dbLaneKey);

        // 변경 전 스냅샷
        ProcessStatus beforeStatus = process.getStatus();
        // beforeOrder: lane_order 기준 (없으면 null)
        Integer beforeOrder = processLaneOrderRepository
                .findByProjectIdAndProcessIdAndLaneKeyAndStatusAndDeletedAtIsNull(projectId, processId, dbLaneKey, beforeStatus)
                .map(ProcessLaneOrder::getSortOrder)
                .orElse(null);
        LocalDate beforeStart = process.getStartAt();
        LocalDate beforeEnd = process.getEndAt();

        // 기간 변경
        LocalDate newStart = req.startDate();
        LocalDate newEnd = req.deadLine();

        if (newStart != null || newEnd != null) {
            LocalDate mergedStart = (newStart != null) ? newStart : process.getStartAt();
            LocalDate mergedEnd = (newEnd != null) ? newEnd : process.getEndAt();

            if (mergedStart != null && mergedEnd != null && mergedStart.isAfter(mergedEnd)) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_PROCESS_PERIOD,
                        "startDate = " + mergedStart + ", endDate = " + mergedEnd
                );
            }

            process.updatePeriod(mergedStart, mergedEnd);
        }

        // 상태 변경(드롭다운/드래그로 상태가 바뀌는 경우)
        if (req.status() != null) {
            process.updateStatus(req.status());
        }

        // 이번 요청이 적용될 status (정렬 검증/업데이트 기준)
        ProcessStatus laneStatus = (req.status() != null) ? req.status() : beforeStatus;

        // 레인 내 표시순서 재정렬
        List<Long> orderedIds = req.orderedProcessIds();
        if (orderedIds != null && !orderedIds.isEmpty()) {
            if (!orderedIds.contains(processId)) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids must contain processId=" + processId
                );
            }

            // 중복 방지
            if (new HashSet<>(orderedIds).size() != orderedIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids contains duplicates"
                );
            }

            // id들이 전부 같은 프로젝트 소속이며 soft delete 제외인지 검증
            List<Process> targets = processRepository.findAllByIdsInProject(projectId, orderedIds);
            if (targets.size() != orderedIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids contains invalid processId(s)"
                );
            }

            // 레인(status) 검증
            boolean invalidLane = targets.stream().anyMatch(p -> p.getStatus() != laneStatus);
            if (invalidLane) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids must contain only processes in status=" + laneStatus
                );
            }

            int laneTotal;

            // Team(전체): 필드 없는 카드만 허용 검증 제거
            if (TEAM_LANE_KEY.equals(dbLaneKey)) {
                laneTotal = processRepository.countByProjectIdAndDeletedAtIsNullAndStatus(projectId, laneStatus);

            } else if (dbLaneKey.startsWith("ROLE:")) {

                String raw = dbLaneKey.substring("ROLE:".length()).trim();
                RoleField rf;
                try {
                    rf = RoleField.valueOf(raw);
                } catch (Exception e) {
                    throw new ProcessException(
                            ProcessErrorCode.INVALID_REQUEST,
                            "invalid lane_key(role). laneKey=" + dbLaneKey
                    );
                }

                if (rf == RoleField.CUSTOM) {
                    throw new ProcessException(
                            ProcessErrorCode.INVALID_REQUEST,
                            "ROLE lane cannot be CUSTOM. laneKey=" + dbLaneKey
                    );
                }

                boolean invalid = targets.stream().anyMatch(p ->
                        p.getProcessFields().stream().noneMatch(pf ->
                                pf.getDeletedAt() == null && pf.getRoleField() == rf
                        )
                );
                if (invalid) {
                    throw new ProcessException(
                            ProcessErrorCode.INVALID_REQUEST,
                            "ordered_process_ids contains process not in role lane. laneKey=" + dbLaneKey
                    );
                }

                laneTotal = processRepository.countRoleLaneTotal(projectId, laneStatus, rf);

            } else if (dbLaneKey.startsWith("CUSTOM:")) {

                String customName = dbLaneKey.substring("CUSTOM:".length()).trim();
                if (customName.isBlank()) {
                    throw new ProcessException(
                            ProcessErrorCode.INVALID_REQUEST,
                            "invalid lane_key(custom). laneKey=" + dbLaneKey
                    );
                }

                boolean invalid = targets.stream().anyMatch(p ->
                        p.getProcessFields().stream().noneMatch(pf ->
                                pf.getDeletedAt() == null
                                        && pf.getRoleField() == RoleField.CUSTOM
                                        && customName.equals(pf.getCustomFieldName())
                        )
                );
                if (invalid) {
                    throw new ProcessException(
                            ProcessErrorCode.INVALID_REQUEST,
                            "ordered_process_ids contains process not in custom lane. laneKey=" + dbLaneKey
                    );
                }

                laneTotal = processRepository.countCustomLaneTotal(projectId, laneStatus, customName);

            } else {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "invalid lane_key prefix. laneKey=" + dbLaneKey
                );
            }

            // 전체 포함 정책
            if (laneTotal != orderedIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids must include all processes in the lane. status=" + laneStatus + ", laneKey=" + apiLaneKeyForHistory
                );
            }

            // 여기부터 lane_order 갱신
            // row 없으면 생성(정렬 대상 targets 기준)
            ensureLaneOrderRowsExist(projectId, laneStatus, dbLaneKey, targets);

            // 현재 lane+status에 해당하는 row들 조회
            List<ProcessLaneOrder> rows =
                    processLaneOrderRepository.findAllByLaneAndProcessIds(projectId, dbLaneKey, laneStatus, orderedIds);

            Map<Long, ProcessLaneOrder> byPid = rows.stream()
                    .collect(Collectors.toMap(r -> r.getProcess().getId(), r -> r));

            // 요청 순서대로 statusOrder 재부여
            int order = 0;
            for (Long id : orderedIds) {
                ProcessLaneOrder row = byPid.get(id);

                if (row == null) {
                    // 방어적으로 생성
                    Process p = targets.stream()
                            .filter(t -> t.getId().equals(id))
                            .findFirst()
                            .orElseThrow();

                    ProcessLaneOrder created = ProcessLaneOrder.builder()
                            .projectId(projectId)
                            .process(p)
                            .laneKey(dbLaneKey)
                            .status(laneStatus)
                            .sortOrder(order)
                            .build();

                    processLaneOrderRepository.save(created);
                } else {
                    row.updateSortOrder(order);
                }
                order++;
            }
        }

        // 변경 후 스냅샷
        ProcessStatus afterStatus = process.getStatus();
        Integer afterOrder = processLaneOrderRepository
                .findByProjectIdAndProcessIdAndLaneKeyAndStatusAndDeletedAtIsNull(projectId, processId, dbLaneKey, afterStatus)
                .map(ProcessLaneOrder::getSortOrder)
                .orElse(null);

        LocalDate afterStart = process.getStartAt();
        LocalDate afterEnd = process.getEndAt();

        boolean changed =
                (beforeStatus != afterStatus) ||
                        !Objects.equals(beforeOrder, afterOrder) ||
                        !Objects.equals(beforeStart, afterStart) ||
                        !Objects.equals(beforeEnd, afterEnd);

        if (changed) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("before", Map.of(
                    "status", beforeStatus,
                    "statusOrder", beforeOrder,
                    "startAt", beforeStart,
                    "endAt", beforeEnd
            ));
            meta.put("after", Map.of(
                    "status", afterStatus,
                    "statusOrder", afterOrder,
                    "startAt", afterStart,
                    "endAt", afterEnd
            ));
            meta.put("laneKey", apiLaneKeyForHistory); // TEAM이면 null
            meta.put("orderedProcessIds", orderedIds);

            historyPublisher.publish(
                    projectId,
                    userId,
                    HistoryAction.PROCESS_REORDERED,
                    HistoryTargetType.PROCESS,
                    processId,
                    meta
            );
        }

        return new ProcessOrderUpdateResDto(
                processId,
                afterStatus,
                afterOrder,
                afterStart,
                afterEnd
        );
    }

    // 프로세스 작업 상태 변경
    @Transactional
    public ProcessStatusUpdateResDto updateProcessStatus(Long projectId, Long userId, Long processId, ProcessStatusUpdateReqDto req) {
        assertActiveProjectMember(projectId, userId);


        if (req == null || req.status() == null) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "status is null");
        }

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));

        ProcessStatus before = process.getStatus();
        ProcessStatus after = req.status();

        // 같은 상태면 그대로 반환
        if (before == after) {
            return new ProcessStatusUpdateResDto(
                    process.getId(),
                    process.getStatus(),
                    process.getUpdatedAt()
            );
        }

        process.updateStatus(after);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("beforeStatus", before);
        meta.put("afterStatus", after);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.PROCESS_STATUS_CHANGED,
                HistoryTargetType.PROCESS,
                process.getId(),
                meta
        );

        return new ProcessStatusUpdateResDto(
                process.getId(),
                process.getStatus(),
                process.getUpdatedAt()
        );
    }

}
