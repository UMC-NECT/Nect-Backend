package com.nect.api.domain.team.process.service;

import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.*;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.enums.AttachmentType;
import com.nect.api.domain.team.process.enums.LaneType;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.ProjectUser;
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
import com.nect.core.repository.team.ProjectTeamRoleRepository;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final ProjectTeamRoleRepository projectTeamRoleRepository;

    private final ProcessLaneOrderService processLaneOrderService;

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

    // lane 내 기간 겹침 검증
    private void validateNoOverlapInLane(Long projectId, ProcessCreateReqDto req, LocalDate start, LocalDate end) {
        List<RoleField> roleFields = Optional.ofNullable(req.roleFields()).orElse(List.of())
                .stream().filter(Objects::nonNull).distinct().toList();

        // ROLE (CUSTOM 제외)
        for (RoleField rf : roleFields) {
            if (rf == RoleField.CUSTOM) continue;

            boolean overlap = processRepository.existsOverlappingInRoleLane(projectId, rf, start, end);
            if (overlap) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_PROCESS_PERIOD,
                        "roleField=" + rf + ", start=" + start + ", end=" + end
                );
            }
        }

        // CUSTOM lane
        if (roleFields.contains(RoleField.CUSTOM)) {
            String custom = (req.customFieldName() == null) ? "" : req.customFieldName().trim();
            if (custom.isBlank()) {
                throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "custom_field_name is required when role_fields contains CUSTOM");
            }

            boolean overlap = processRepository.existsOverlappingInCustomLane(projectId, custom, start, end);
            if (overlap) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_PROCESS_PERIOD,
                        "customName=" + custom + ", start=" + start + ", end=" + end
                );
            }
        }
    }

    private void validateNoOverlapForUpdateBasic(
            Long projectId,
            Long processId,
            List<RoleField> roleFields,
            List<String> customFields,
            LocalDate start,
            LocalDate end
    ) {
        // ROLE (CUSTOM 제외)
        for (RoleField rf : Optional.ofNullable(roleFields).orElse(List.of())) {
            if (rf == null || rf == RoleField.CUSTOM) continue;

            boolean overlap = processRepository.existsOverlappingInRoleLaneExcludingProcess(
                    projectId, rf, start, end, processId
            );
            if (overlap) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_PROCESS_PERIOD,
                        "roleField=" + rf + ", start=" + start + ", end=" + end
                );
            }
        }

        // CUSTOM lanes (이름 기반)
        for (String name : Optional.ofNullable(customFields).orElse(List.of())) {
            if (name == null) continue;
            String trimmed = name.trim();
            if (trimmed.isBlank()) continue;

            boolean overlap = processRepository.existsOverlappingInCustomLaneExcludingProcess(
                    projectId, trimmed, start, end, processId
            );
            if (overlap) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_PROCESS_PERIOD,
                        "customName=" + trimmed + ", start=" + start + ", end=" + end
                );
            }
        }
    }

    private void validateNoOverlapForUpdateOrderLane(
            Long projectId,
            Long processId,
            String dbLaneKey,
            LocalDate start,
            LocalDate end
    ) {
        if (TEAM_LANE_KEY.equals(dbLaneKey)) return;

        if (dbLaneKey.startsWith("ROLE:")) {
            RoleField rf = parseRoleField(dbLaneKey);
            if (rf == RoleField.CUSTOM) {
                throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "ROLE lane cannot be CUSTOM. laneKey=" + dbLaneKey);
            }

            boolean overlap = processRepository.existsOverlappingInRoleLaneExcludingProcess(
                    projectId, rf, start, end, processId
            );
            if (overlap) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_PROCESS_PERIOD,
                        "roleField=" + rf + ", start=" + start + ", end=" + end
                );
            }
            return;
        }

        if (dbLaneKey.startsWith("CUSTOM:")) {
            String customName = parseCustomName(dbLaneKey);

            boolean overlap = processRepository.existsOverlappingInCustomLaneExcludingProcess(
                    projectId, customName, start, end, processId
            );
            if (overlap) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_PROCESS_PERIOD,
                        "customName=" + customName + ", start=" + start + ", end=" + end
                );
            }
            return;
        }

        throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "invalid lane_key prefix. laneKey=" + dbLaneKey);
    }


    // 선택 미션 N과 startDate 포함 검증
    private void validateStartDateInSelectedMission(Long projectId, Integer missionNumber, LocalDate startDate) {
        if (missionNumber == null) return;

        var mp = processRepository.findWeekMissionPeriodByMissionNumber(projectId, missionNumber)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "week mission not found. missionNumber=" + missionNumber
                ));

        LocalDate mStart = mp.getStartAt();
        LocalDate mEnd = mp.getEndAt();
        if (mStart == null || mEnd == null) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "missionNumber=" + missionNumber);
        }

        boolean ok = !startDate.isBefore(mStart) && !startDate.isAfter(mEnd);
        if (!ok) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_PROCESS_PERIOD,
                    "startDate=" + startDate + ", mission=" + missionNumber
            );
        }
    }

    private void validateProjectTeamRolesOrThrow(Long projectId, List<RoleField> roleFields, String customFieldName) {

        // roleFields(일반 역할) 검증
        for (RoleField rf : Optional.ofNullable(roleFields).orElse(List.of())) {
            if (rf == null) continue;

            if (rf == RoleField.CUSTOM) continue; // CUSTOM은 customFieldName으로 검증

            boolean exists = projectTeamRoleRepository.existsByProject_IdAndRoleField(projectId, rf);
            if (!exists) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "role_field not registered in project. roleField=" + rf
                );
            }
        }

        // CUSTOM 검증 (ProcessCreateReqDto는 customFieldName 단일)
        if (roleFields != null && roleFields.contains(RoleField.CUSTOM)) {
            String name = (customFieldName == null) ? "" : customFieldName.trim();
            if (name.isBlank()) {
                throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "custom_field_name is required");
            }

            boolean exists = projectTeamRoleRepository
                    .existsByProject_IdAndRoleFieldAndCustomRoleFieldName(projectId, RoleField.CUSTOM, name);

            if (!exists) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "custom role not registered in project. customRoleFieldName=" + name
                );
            }
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
                NotificationScope.WORKSPACE_ONLY,
                targetProcessId,
                new Object[]{ actor.getName() },
                new Object[]{ content },
                project
        );

        notificationFacade.notify(receivers, command);
    }

    // 프로세스 생성 서비스
    @Transactional
    public ProcessCreateResDto createProcess(Long projectId, Long userId, ProcessCreateReqDto req) {

        assertActiveProjectMember(projectId, userId);
        validateProcessTitle(req.processTitle());

        List<ProcessTaskItemReqDto> taskItems = Optional.ofNullable(req.taskItems()).orElse(List.of());
        validateTaskItems(taskItems);

        // 프로젝트 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROJECT_NOT_FOUND,
                        "projectId = " + projectId
                ));

        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.USER_NOT_FOUND,
                        "userId = " + userId
                ));

        // Process 기본 생성
        Process process = Process.builder()
                .project(project)
                .createdBy(writer)
                .title(req.processTitle())
                .content(req.processContent())
                .build();

        // status 기본값 처리
        ProcessStatus status = (req.processStatus() == null) ? ProcessStatus.PLANNING : req.processStatus();
        process.updateStatus(status);

        // 기간 설정
        LocalDate start = req.startDate();
        LocalDate end = req.deadLine();

        if (start == null || end == null) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_REQUEST,
                    "startDate and deadLine are required"
            );
        }


        if (start.isAfter(end)) {
            throw new ProcessException(
                    ProcessErrorCode.INVALID_PROCESS_PERIOD,
                    "startDate = " + start + ", deadLine = " + end
            );
        }

        process.updatePeriod(start, end);

        // 필드(역할) CUSTOM쪽 검증
        List<RoleField> roleFields = Optional.ofNullable(req.roleFields()).orElse(List.of())
                .stream().filter(Objects::nonNull).distinct().toList();

        // 정규화 이후 검증/저장/히스토리 모두 이 값 사용
        String customName = (req.customFieldName() == null) ? null : req.customFieldName().trim();

        // 프로젝트에 등록된 파트인지 검증 (CUSTOM 포함)
        validateProjectTeamRolesOrThrow(projectId, roleFields, req.customFieldName());

        // 미션 N 검증: "시작일만" 미션 기간에 포함되면 요구사항 일치
        validateStartDateInSelectedMission(projectId, req.missionNumber(), start);

        // lane 기간 겹침 검증
        validateNoOverlapInLane(projectId, req, start, end);

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
        List<ProcessCreateReqDto.ProcessLinkItemReqDto> links =
                Optional.ofNullable(req.links()).orElse(List.of());

        for (var l : links) {
            if (l == null) continue;

            String title = (l.title() == null) ? "" : l.title().trim();
            String url = (l.url() == null) ? "" : l.url().trim();

            if (title.isBlank() || url.isBlank()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "link title and url are required"
                );
            }

            Link link = Link.builder()
                    .title(title)
                    .url(url)
                    .build();

            process.addLink(link);
        }


        for (RoleField rf : roleFields) {
            if (rf == RoleField.CUSTOM) process.addField(RoleField.CUSTOM, customName);
            else process.addField(rf, null);
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
                writer,
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
        meta.put("customFieldName", roleFields.contains(RoleField.CUSTOM) ? customName : null);
        meta.put("assigneeIds", assigneeIds);
        meta.put("mentionUserIds", mentionIds);
        meta.put("fileIds", fileIds);
        List<Map<String, String>> linkMetas = links.stream()
                .filter(Objects::nonNull)
                .map(l -> Map.of(
                        "title", l.title() == null ? "" : l.title().trim(),
                        "url", l.url() == null ? "" : l.url().trim()
                ))
                .toList();

        meta.put("links", linkMetas);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.PROCESS_CREATED,
                HistoryTargetType.PROCESS,
                saved.getId(),
                meta
        );

        ProjectUser writerMember = projectUserRepository.findByUserIdAndProject(writer.getUserId(), project)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "writer must be active project member. projectId=" + projectId + ", userId=" + userId
                ));

        List<ProcessCreateResDto.AssigneeDto> assigneeDtos =
                saved.getProcessUsers().stream()
                        .filter(pu -> pu.getDeletedAt() == null)
                        .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                        .map(pu -> {
                            User u = pu.getUser();
                            return new ProcessCreateResDto.AssigneeDto(
                                    u.getUserId(),
                                    u.getName(),
                                    u.getNickname(),
                                    u.getProfileImageUrl()
                            );
                        })
                        .toList();

        return new ProcessCreateResDto(
                saved.getId(),
                saved.getCreatedAt(),
                new ProcessCreateResDto.WriterDto( // 작성자
                        writer.getUserId(),
                        writer.getName(),
                        writer.getNickname(),
                        writerMember.getRoleField(),
                        writerMember.getCustomRoleFieldName()
                ),
                assigneeDtos // 담당자
        );
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

        List<ProcessDetailResDto.AttachmentDto> fileAttachments =
                process.getSharedDocuments().stream()
                        .filter(psd -> psd.getDeletedAt() == null)
                        .map(psd -> {
                            SharedDocument doc = psd.getDocument();
                            if (doc == null) return null;

                            // 정렬 기준 시간: attachedAt 우선, 없으면 psd createdAt fallback
                            LocalDateTime at =
                                    psd.getAttachedAt() != null ? psd.getAttachedAt() : psd.getCreatedAt();

                            return new ProcessDetailResDto.AttachmentDto(
                                    AttachmentType.FILE,
                                    doc.getId(),
                                    at,
                                    null, null,
                                    doc.getFileName(),
                                    doc.getFileUrl(),
                                    doc.getFileExt(),
                                    doc.getFileSize()
                            );
                        })
                        .filter(Objects::nonNull)
                        .toList();

        List<ProcessDetailResDto.AttachmentDto> linkAttachments =
                process.getLinks().stream()
                        .filter(l -> l.getDeletedAt() == null)
                        .map(l -> new ProcessDetailResDto.AttachmentDto(
                                AttachmentType.LINK,
                                l.getId(),
                                l.getCreatedAt(),
                                l.getTitle(),
                                l.getUrl(),
                                null, null, null, null
                        ))
                        .toList();

        List<ProcessDetailResDto.AttachmentDto> attachments =
                Stream.concat(fileAttachments.stream(), linkAttachments.stream())
                        .filter(a -> a.createdAt() != null)
                        .sorted(Comparator.comparing(ProcessDetailResDto.AttachmentDto::createdAt).reversed())
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

                    String userImage = u.getProfileImageUrl();

                    return new AssigneeResDto(
                            u.getUserId(),
                            u.getName(),
                            u.getNickname(),
                            userImage
                    );
                })
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
                            createdBy == null ? null : createdBy.getNickname(),
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
                taskItems,
                feedbacks,
                attachments,
                process.getCreatedAt(),
                process.getUpdatedAt(),
                process.getDeletedAt()
        );
    }

    private List<Long> syncMentions(Process process, List<Long> requestedUserIds) {
        // null이면 언급 변경 안 함
        if (requestedUserIds == null) return null;

        // 빈 리스트면 언급 전부 제거 의미(=req가 빈 리스트)
        List<Long> req = requestedUserIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 삭제 포함 전체 조회
        List<ProcessMention> all = processMentionRepository.findAllByProcessId(process.getId());

        // 기존 mention을 userId 기준으로 맵핑 (null/중복 방어)
        Map<Long, ProcessMention> byUserId = new HashMap<>();
        for (ProcessMention m : all) {
            Long mid = m.getMentionedUserId();
            if (mid == null) continue;

            ProcessMention prev = byUserId.putIfAbsent(mid, m);
            if (prev != null) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "duplicated mention row. mentionedUserId=" + mid + ", processId=" + process.getId()
                );
            }
        }

        Set<Long> reqSet = new HashSet<>(req);

        // 요청에 없는 기존 mention -> soft delete
        for (ProcessMention m : all) {
            Long mid = m.getMentionedUserId();
            if (mid == null) continue;

            if (!reqSet.contains(mid)) {
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
                .filter(rf -> rf != RoleField.CUSTOM)
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
            validateProcessTitle(req.processTitle());
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

        // 시작일만 미션 범위에 포함되면 통과
        if (req.missionNumber() != null && mergedStart != null && mergedEnd != null) {
            validateStartDateInSelectedMission(projectId, req.missionNumber(), mergedStart);
        }

        // fields PATCH 준비(정규화 + 프로젝트 등록 파트 검증)
        boolean fieldsPatchRequested = (req.roleFields() != null || req.customFields() != null);

        List<RoleField> requestedRoleFields = (req.roleFields() == null)
                ? null
                : req.roleFields().stream()
                .filter(Objects::nonNull)
                .filter(rf -> rf != RoleField.CUSTOM)
                .distinct()
                .toList();

        List<String> requestedCustomFields = (req.customFields() == null)
                ? null
                : normalizeCustomFields(req.customFields());

        if (fieldsPatchRequested) {
            validateProjectTeamRolesForUpdateOrThrow(
                    projectId,
                    (requestedRoleFields == null ? List.of() : requestedRoleFields),
                    (requestedCustomFields == null ? List.of() : requestedCustomFields)
            );
        }

        // 기간 변경이 없더라도 "파트/커스텀 변경"만으로도 lane이 바뀌면 overlap 가능
        boolean periodPatchRequested = (newStart != null || newEnd != null);

        if (mergedStart != null && mergedEnd != null && (periodPatchRequested || fieldsPatchRequested)) {

            List<RoleField> laneRoleFields = (requestedRoleFields != null)
                    ? requestedRoleFields
                    : process.getProcessFields().stream()
                    .filter(pf -> pf.getDeletedAt() == null)
                    .map(ProcessField::getRoleField)
                    .filter(Objects::nonNull)
                    .filter(rf -> rf != RoleField.CUSTOM)
                    .distinct()
                    .toList();

            List<String> laneCustomFields = (requestedCustomFields != null)
                    ? requestedCustomFields
                    : process.getProcessFields().stream()
                    .filter(pf -> pf.getDeletedAt() == null)
                    .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                    .map(ProcessField::getCustomFieldName)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .toList();

            validateNoOverlapForUpdateBasic(projectId, processId, laneRoleFields, laneCustomFields, mergedStart, mergedEnd);
        }


        if (periodPatchRequested) {
            process.updatePeriod(mergedStart, mergedEnd);
        }


        // 멘션 교체
        List<Long> mentionIdsForRes = syncMentions(process, req.mentionUserIds());

        // 멘션 알림: 요청이 들어온 경우에만, 그리고 '새로 추가된 멘션'에게만 전송
        if (req.mentionUserIds() != null) {
            List<Long> afterIds = (mentionIdsForRes == null) ? List.of()
                    : mentionIdsForRes.stream().filter(Objects::nonNull).distinct().toList();

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

        if (fieldsPatchRequested) {
            // 기존 전부 soft delete
            process.getProcessFields().forEach(pf -> {
                if (pf.getDeletedAt() == null) pf.softDelete();
            });

            List<RoleField> finalRoleFields = (requestedRoleFields == null) ? List.of() : requestedRoleFields;
            for (RoleField rf : finalRoleFields) {
                ProcessField found = process.getProcessFields().stream()
                        .filter(pf -> pf.getRoleField() == rf)
                        .findFirst()
                        .orElse(null);

                if (found != null) found.restore();
                else process.getProcessFields().add(ProcessField.builder()
                        .process(process)
                        .roleField(rf)
                        .customFieldName(null)
                        .build());
            }

            List<String> finalCustomFields = (requestedCustomFields == null) ? List.of() : requestedCustomFields;
            for (String name : finalCustomFields) {
                ProcessField found = process.getProcessFields().stream()
                        .filter(pf -> pf.getRoleField() == RoleField.CUSTOM)
                        .filter(pf -> pf.getCustomFieldName() != null && pf.getCustomFieldName().trim().equals(name))
                        .findFirst()
                        .orElse(null);

                if (found != null) found.restore();
                else process.getProcessFields().add(ProcessField.builder()
                        .process(process)
                        .roleField(RoleField.CUSTOM)
                        .customFieldName(name)
                        .build());
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

        List<AssigneeResDto> assigneeDtos = process.getProcessUsers().stream()
                .filter(pu -> pu.getDeletedAt() == null)
                .filter(pu -> pu.getAssignmentRole() == AssignmentRole.ASSIGNEE)
                .map(pu -> {
                    User u = pu.getUser();
                    return new AssigneeResDto(
                            u.getUserId(),
                            u.getName(),
                            u.getNickname(),
                            u.getProfileImageUrl()
                    );
                })
                .toList();

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

        User writer = process.getCreatedBy();

        ProjectUser writerMember = projectUserRepository
                .findByUserIdAndProject(writer.getUserId(), process.getProject())
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "writer must be active project member. projectId=" + projectId + ", userId=" + writer.getUserId()
                ));

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
                assigneeDtos,
                (afterMentionIds == null) ? beforeMentionIds : afterMentionIds,
                process.getUpdatedAt(),
                new ProcessBasicUpdateResDto.WriterDto(
                        writer.getUserId(),
                        writer.getName(),
                        writer.getNickname(),
                        writerMember.getRoleField(),
                        writerMember.getCustomRoleFieldName()
                )
        );

    }

    private List<String> normalizeCustomFields(List<String> raw) {
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    /**
     * 프로젝트에 등록된 파트(ProjectTeamRole)인지 검증
     * - roleFields: CUSTOM 제외 리스트
     * - customFields: CUSTOM 이름 리스트
     */
    private void validateProjectTeamRolesForUpdateOrThrow(Long projectId, List<RoleField> roleFields, List<String> customFields) {
        List<ProjectTeamRoleRepository.TeamRoleRow> rows =
                projectTeamRoleRepository.findActiveTeamRoleRowsByProjectId(projectId);

        Set<RoleField> registeredRoleFields = rows.stream()
                .map(ProjectTeamRoleRepository.TeamRoleRow::getRoleField)
                .filter(Objects::nonNull)
                .filter(rf -> rf != RoleField.CUSTOM)
                .collect(Collectors.toSet());

        Set<String> registeredCustomNames = rows.stream()
                .filter(r -> r.getRoleField() == RoleField.CUSTOM)
                .map(ProjectTeamRoleRepository.TeamRoleRow::getCustomRoleFieldName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        for (RoleField rf : roleFields) {
            if (!registeredRoleFields.contains(rf)) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "role_field not registered in project. projectId=" + projectId + ", roleField=" + rf
                );
            }
        }

        for (String name : customFields) {
            if (!registeredCustomNames.contains(name)) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "custom_field not registered in project. projectId=" + projectId + ", customField=" + name
                );
            }
        }
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

    private LocalDate normalizeWeekStart(LocalDate date) {
        if (date == null) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "startDate must not be null");
        }
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
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
                    String userImage = u.getProfileImageUrl();
                    String nickname = u.getNickname();
                    return new AssigneeResDto(u.getUserId(), u.getName(), nickname, userImage);
                })
                .toList();

        Integer missionNumber = resolveMissionNumberByStartDate(p.getProject().getId(), p.getStartAt());

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
                missionNumber,
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


    private LocalDate toMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate resolveWeekStart(LocalDate requested, LocalDate fallbackBaseDate) {
        LocalDate base = (requested != null) ? requested : fallbackBaseDate;
        return toMonday(base);
    }

    private Integer resolveMissionNumberByStartDate(Long projectId, LocalDate startAt) {
        if (startAt == null) return null;

        return processRepository.findWeekMissionContainingDate(projectId, startAt)
                .map(Process::getMissionNumber)
                .orElse(null);
    }

    // 주차별 프로세스 조회 서비스
    @Transactional(readOnly = true)
    public ProcessWeeksResDto getWeekProcesses(Long projectId, Long userId, LocalDate startDate, int weeks) {

        assertActiveProjectMember(projectId, userId);

        if (!projectRepository.existsById(projectId)) {
            throw new ProcessException(
                    ProcessErrorCode.PROJECT_NOT_FOUND,
                    "projectId=" + projectId
            );
        }

        if (weeks <= 0) weeks = 1;
        if (weeks > 12) weeks = 12;

        LocalDate fallback = processRepository.findMinProcessStartAt(projectId);
        if (fallback == null) fallback = LocalDate.now();

        LocalDate rangeStart = resolveWeekStart(startDate, fallback);
        LocalDate rangeEnd = rangeStart.plusDays((long) weeks * 7 - 1);

        List<Process> processes = processRepository.findAllInRangeOrdered(projectId, rangeStart, rangeEnd);

        // 프로세스를 주차별로 묶기
        // startAt이 null이면 rangeStart 주로 보내거나, common 처리 가능
        Map<LocalDate, List<Process>> byWeek = new LinkedHashMap<>();
        for (int i = 0; i < weeks; i++) {
            LocalDate ws = rangeStart.plusWeeks(i);
            byWeek.put(ws, new java.util.ArrayList<>());
        }

        LocalDate lastWeekStart = rangeStart.plusWeeks(weeks - 1);

        for (Process p : processes) {
            LocalDate key = (p.getStartAt() == null) ? rangeStart : normalizeWeekStart(p.getStartAt());

            if (key.isBefore(rangeStart)) key = rangeStart;
            if (key.isAfter(lastWeekStart)) key = lastWeekStart;

            byWeek.get(key).add(p);
        }


        List<ProcessWeekResDto> weekDtos = byWeek.entrySet().stream()
                .map(entry -> {
                    LocalDate weekStartKey = entry.getKey();
                    List<ProcessCardResDto> cards = entry.getValue().stream()
                            .map(this::toProcessCardResDTO)
                            .toList();
                    return buildWeekDto(weekStartKey, cards);
                })
                .toList();

        return new ProcessWeeksResDto(weekDtos);

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

    private List<Process> fetchProcessesByIds(Long projectId, List<Long> ids, boolean needFields) {
        if (ids == null || ids.isEmpty()) return List.of();

        List<Process> processes = processRepository.findAllByIdsInProjectWithUsers(projectId, ids);

        if (needFields) {
            processRepository.findAllByIdsInProjectWithFields(projectId, ids);
        }

        return processes;
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

        // lane 대상 프로세스 목록
        List<Process> laneProcesses = List.of();

        if (TEAM_LANE_KEY.equals(dbLaneKey)) { // 팀(전체)
            laneProcesses = processRepository.findAllForTeamBoard(projectId);
        }else if (isRoleLane(dbLaneKey)) { // 역할
            RoleField roleField = parseRoleField(dbLaneKey);
            if (roleField == RoleField.CUSTOM) {
                throw new ProcessException(ProcessErrorCode.INVALID_REQUEST, "ROLE lane cannot be CUSTOM. laneKey=" + laneKey);
            }
            List<Long> ids = processRepository.findRoleLaneIds(projectId, roleField);
            laneProcesses = fetchProcessesByIds(projectId, ids, false);
        }else if (isCustomLane(dbLaneKey)) { // CUSTOM(직접 입력)
            String customName = parseCustomName(dbLaneKey);
            List<Long> ids = processRepository.findCustomLaneIds(projectId, customName);
            laneProcesses = fetchProcessesByIds(projectId, ids, false);
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
        processLaneOrderService.ensureLaneOrderRowsExistWriteTx(
                projectId, status, laneKey, laneProcesses
        );

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
    public ProcessProgressSummaryResDto  getPartProgressSummary(Long projectId, Long userId) {
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

            if (req.missionNumber() != null && mergedStart != null) {
                validateStartDateInSelectedMission(projectId, req.missionNumber(), mergedStart);
            }

            if (mergedStart != null && mergedEnd != null) {
                validateNoOverlapForUpdateOrderLane(projectId, processId, dbLaneKey, mergedStart, mergedEnd);
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

            // 레인 검증
            boolean invalidLane = targets.stream().anyMatch(p -> p.getStatus() != laneStatus);
            if (invalidLane) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids must contain only processes in status=" + laneStatus
                );
            }

            // lane 검증 + laneTotal 검증
            int laneTotal = validateLaneAndCountTotal(projectId, laneStatus, dbLaneKey, targets);
            if (laneTotal != orderedIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids must include all processes in the lane. status=" + laneStatus + ", laneKey=" + apiLaneKeyForHistory
                );
            }

            // 요청 lane 자체 리오더
            reorderLane(projectId, laneStatus, dbLaneKey, orderedIds, targets);

            // 양방향 동기화
            if (TEAM_LANE_KEY.equals(dbLaneKey)) {
                // TEAM에서 reorder, 다른 lane들을 TEAM 기준으로 계산
                propagateTeamOrderToAllLanes(projectId, laneStatus);
            } else {
                // ROLE/CUSTOM에서 reorder, TEAM을 “부분 재정렬”로 갱신 후 TEAM 기준으로 전파
                applySubsetLaneOrderToTeam(projectId, laneStatus, orderedIds);
                propagateTeamOrderToAllLanes(projectId, laneStatus);
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
                        !java.util.Objects.equals(beforeOrder, afterOrder) ||
                        !java.util.Objects.equals(beforeStart, afterStart) ||
                        !java.util.Objects.equals(beforeEnd, afterEnd);

        if (changed) {
            java.util.Map<String, Object> meta = new java.util.LinkedHashMap<>();
            meta.put("before", java.util.Map.of(
                    "status", beforeStatus,
                    "statusOrder", beforeOrder,
                    "startAt", beforeStart,
                    "endAt", beforeEnd
            ));
            meta.put("after", java.util.Map.of(
                    "status", afterStatus,
                    "statusOrder", afterOrder,
                    "startAt", afterStart,
                    "endAt", afterEnd
            ));
            meta.put("laneKey", apiLaneKeyForHistory);
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

    private int validateLaneAndCountTotal(
            Long projectId,
            ProcessStatus laneStatus,
            String dbLaneKey,
            List<Process> targets
    ) {
        // TEAM: status 내 전체 프로세스 수
        if (TEAM_LANE_KEY.equals(dbLaneKey)) {
            return processRepository.countByProjectIdAndDeletedAtIsNullAndStatus(projectId, laneStatus);
        }

        // ROLE
        if (dbLaneKey.startsWith("ROLE:")) {
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

            // ROLE lane은 CUSTOM 금지
            if (rf == RoleField.CUSTOM) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ROLE lane cannot be CUSTOM. laneKey=" + dbLaneKey
                );
            }

            // targets가 해당 ROLE lane에 속하는지 검증
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

            return processRepository.countRoleLaneTotal(projectId, laneStatus, rf);
        }

        // CUSTOM
        if (dbLaneKey.startsWith("CUSTOM:")) {
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

            return processRepository.countCustomLaneTotal(projectId, laneStatus, customName);
        }

        // prefix 자체가 이상
        throw new ProcessException(
                ProcessErrorCode.INVALID_REQUEST,
                "invalid lane_key prefix. laneKey=" + dbLaneKey
        );
    }


    /**
     * ROLE/CUSTOM lane의 orderedIds를 TEAM에 “부분 재정렬”로 반영한다
     * TEAM에서 subset만 재배치하고, 나머지는 그대로 유지
     */
    private void applySubsetLaneOrderToTeam(Long projectId, ProcessStatus status, List<Long> laneOrderedIds) {
        // TEAM의 현재 전체 순서(해당 status)
        List<Long> teamOrderedIds = processLaneOrderRepository.findOrderedProcessIds(projectId, TEAM_LANE_KEY, status);

        if (teamOrderedIds == null || teamOrderedIds.isEmpty()) {
            // TEAM lane_order가 비어있으면 fallback: status 내 프로세스 조회 -> lane_order row 생성 -> 다시 로드
            List<Process> teamTargets = processRepository.findAllByStatusInProject(projectId, status);
            ensureLaneOrderRowsExist(projectId, status, TEAM_LANE_KEY, teamTargets);
            teamOrderedIds = processLaneOrderRepository.findOrderedProcessIds(projectId, TEAM_LANE_KEY, status);
        }

        Set<Long> subset = new HashSet<>(laneOrderedIds);

        // TEAM에 없는 id가 laneOrderedIds에 있으면 비정상(같은 프로젝트+status 검증에서 이미 걸러졌지만 방어)
        if (!teamOrderedIds.containsAll(laneOrderedIds)) {
            throw new ProcessException(ProcessErrorCode.INVALID_REQUEST,
                    "lane ordered ids not contained in TEAM lane order. status=" + status);
        }

        // TEAM을 subset 위치만 laneOrderedIds 순서로 교체
        List<Long> newTeam = new ArrayList<>(teamOrderedIds.size());
        int idx = 0;
        for (Long tid : teamOrderedIds) {
            if (subset.contains(tid)) {
                newTeam.add(laneOrderedIds.get(idx++));
            } else {
                newTeam.add(tid);
            }
        }

        // TEAM reorder 실행
        List<Process> teamTargets = processRepository.findAllByIdsInProject(projectId, newTeam);
        reorderLane(projectId, status, TEAM_LANE_KEY, newTeam, teamTargets);
    }

    /**
     * TEAM lane_order를 기준으로 “모든 ROLE/CUSTOM lane”의 lane_order를 재계산한다.
     * - 각 lane의 프로세스를 TEAM 순서대로 정렬 후 lane_order 갱신
     */
    private void propagateTeamOrderToAllLanes(Long projectId, ProcessStatus status) {
        List<Long> teamOrderedIds = processLaneOrderRepository.findOrderedProcessIds(projectId, TEAM_LANE_KEY, status);
        if (teamOrderedIds == null || teamOrderedIds.isEmpty()) return;

        java.util.Map<Long, Integer> pos = new java.util.HashMap<>();
        for (int i = 0; i < teamOrderedIds.size(); i++) pos.put(teamOrderedIds.get(i), i);

        // 프로젝트 내 lane 목록(ROLE + CUSTOM)을 한 번에 수집
        List<ProcessRepository.LaneKeyRow> lanes = processRepository.findLaneKeysInProject(projectId);

        for (ProcessRepository.LaneKeyRow row : lanes) {
            String laneKey = toLaneKey(row);
            if (laneKey == null) continue;

            List<Process> laneProcesses = fetchLaneProcesses(projectId, status, laneKey);
            if (laneProcesses.isEmpty()) continue;

            // TEAM 순서(pos) 기준으로 정렬
            laneProcesses.sort(Comparator
                    .comparingInt((Process p) -> pos.getOrDefault(p.getId(), Integer.MAX_VALUE))
                    .thenComparingLong(Process::getId)
            );

            List<Long> newLaneOrder = laneProcesses.stream().map(Process::getId).toList();
            reorderLane(projectId, status, laneKey, newLaneOrder, laneProcesses);
        }
    }

    private String toLaneKey(ProcessRepository.LaneKeyRow row) {
        if (row.getRoleField() == null) return null;

        if (row.getRoleField() == RoleField.CUSTOM) {
            String name = row.getCustomFieldName();
            if (name == null) return null;
            String trimmed = name.trim();
            if (trimmed.isBlank()) return null;
            return "CUSTOM:" + trimmed;
        }
        // CUSTOM 제외 ROLE
        return "ROLE:" + row.getRoleField().name();
    }

    private List<Process> fetchLaneProcesses(Long projectId, ProcessStatus status, String laneKey) {
        if (TEAM_LANE_KEY.equals(laneKey)) {
            return processRepository.findAllByStatusInProject(projectId, status);
        }
        if (laneKey.startsWith("ROLE:")) {
            String raw = laneKey.substring("ROLE:".length()).trim();
            RoleField rf;
            try {
                rf = RoleField.valueOf(raw);
            } catch (Exception e) {
                return List.of();
            }
            if (rf == RoleField.CUSTOM) return List.of();
            return processRepository.findAllInRoleLaneByStatus(projectId, status, rf);
        }
        if (laneKey.startsWith("CUSTOM:")) {
            String name = laneKey.substring("CUSTOM:".length()).trim();
            if (name.isBlank()) return List.of();
            return processRepository.findAllInCustomLaneByStatus(projectId, status, name);
        }
        return List.of();
    }

    // lane_order 갱신
    private void reorderLane(
            Long projectId,
            ProcessStatus status,
            String laneKey,
            List<Long> orderedIds,
            List<Process> targets
    ) {
        ensureLaneOrderRowsExist(projectId, status, laneKey, targets);

        List<ProcessLaneOrder> rows =
                processLaneOrderRepository.findAllByLaneAndProcessIds(projectId, laneKey, status, orderedIds);

        Map<Long, ProcessLaneOrder> byPid = rows.stream()
                .collect(Collectors.toMap(r -> r.getProcess().getId(), r -> r));

        int order = 0;
        for (Long id : orderedIds) {
            ProcessLaneOrder row = byPid.get(id);
            if (row == null) {
                Process p = targets.stream()
                        .filter(t -> t.getId().equals(id))
                        .findFirst()
                        .orElseThrow();

                ProcessLaneOrder created = ProcessLaneOrder.builder()
                        .projectId(projectId)
                        .process(p)
                        .laneKey(laneKey)
                        .status(status)
                        .sortOrder(order)
                        .build();
                processLaneOrderRepository.save(created);
            } else {
                row.updateSortOrder(order);
            }
            order++;
        }
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
