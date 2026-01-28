package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.process.dto.req.ProcessBasicUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessOrderUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessStatusUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.api.notifications.facade.NotificationFacade;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.process.Link;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessMention;
import com.nect.core.entity.team.process.ProcessTaskItem;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.process.ProcessMentionRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProcessRepository processRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final ProcessMentionRepository processMentionRepository;

    private final NotificationFacade notificationFacade;

    // TODO: Field 연동 시 주입
    // private final FieldRepository fieldRepository;

    // TODO: 담당자(assignee) 연동 시 주입
    // private final UserRepository userRepository;

    // TODO(인증/인가): Security/User 붙이면 CurrentUserProvider(또는 AuthFacade), ProjectUserRepository(멤버십 검증용) 주입 예정

    // TODO : 알림 기능 추가 예정

    /**
     * [HISTORY EVENT 발행 정책 - TODO]
     *
     * 목표:
     * - Process 도메인(CRUD/상태변경/정렬변경)은 "히스토리 저장"을 직접 하지 않고,
     *   변경 사실을 ProjectHistoryEvent로만 발행한다. (느슨한 결합 / 모듈 분리)
     *
     * 이벤트 저장 책임:
     * - history 모듈(ProjectHistoryEventHandler)이 @TransactionalEventListener(phase = AFTER_COMMIT)로 이벤트를 받아
     *   project_history 테이블에 저장한다.
     *   (트랜잭션이 성공적으로 커밋된 경우에만 히스토리가 남도록 보장)
     *
     * 발행 시점/조건 :
     * - "요청이 들어옴"이 아니라 "실제로 값이 변경됨"을 기준으로 publish 한다.
     *   - updateProcessBasic: before snapshot -> 변경 적용 -> after 비교 -> changed=true일 때만 publish
     *   - updateProcessOrder: before snapshot(status/order/start/end) -> 변경 적용 -> after 비교 -> changed=true일 때만 publish
     *   - updateProcessStatus: before != after 일 때만 publish
     *   - deleteProcess: softDelete()로 deletedAt 세팅 후 publish
     *
     * metaJson 규격(권장):
     * - action 별로 최소한의 before/after 또는 핵심 delta를 담는다.
     *   - PROCESS_CREATED: {title, status, startAt, endAt}
     *   - PROCESS_UPDATED: {changed:{...}, before:{...}, after:{...}}
     *   - PROCESS_DELETED: {title?, status?, deletedAt}
     *   - PROCESS_STATUS_CHANGED: {beforeStatus, afterStatus, source?}
     *   - PROCESS_REORDERED: {before:{status,statusOrder,startAt,endAt}, after:{...}, orderedProcessIds, source?}
     *
     * actorUserId:
     * - 현재는 임시(1L). 인증 도입 후 CurrentUserProvider/AuthFacade 등에서 userId를 가져와 event에 주입한다.
     */

    // 프로세스 생성 서비스
    @Transactional
    public Long createProcess(Long projectId, ProcessCreateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출 (UserDetails/JWT)
        // TODO(인가): projectId에 현재 유저가 멤버인지 검증 (프로젝트 참여자만 생성 가능)

        // 프로젝트 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROJECT_NOT_FOUND,
                        "projectId = " + projectId
                ));

        // Process 기본 생성
        Process process = Process.builder()
                .project(project)
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
        for (var t : req.taskItems()) {
            Integer order = (t.sortOrder() != null) ? t.sortOrder() : i++;
            ProcessTaskItem item = ProcessTaskItem.builder()
                    .process(process)
                    .content(t.content())
                    .isDone(Boolean.TRUE.equals(t.isDone()))
                    .sortOrder(order)
                    .build();

            process.addTaskItem(item);
        }

        for (Long fileId : req.fileIds()) {
            SharedDocument doc = sharedDocumentRepository.findByIdAndProjectIdAndDeletedAtIsNull(fileId, projectId)
                    .orElseThrow(() -> new ProcessException(
                            ProcessErrorCode.SHARED_DOCUMENT_NOT_FOUND,
                            "projectId=" + projectId + ", fileId=" + fileId
                    ));
            process.attachDocument(doc);
        }

        for (String url : req.links()) {
            if (url == null || url.isBlank()) continue;
            Link link = Link.builder()
                    .process(process)
                    .url(url)
                    .build();

            process.addLink(link);
        }

        // TODO: Field/Assignee 구조 확정되면 구현

        Process saved = processRepository.save(process);

        List<Long> mentionIds = Optional.ofNullable(req.mentionUserIds())
                .orElse(List.of())
                .stream().filter(Objects::nonNull).distinct().toList();

        syncMentions(saved, mentionIds);

        // TODO(HISTORY): 생성 성공 후(Project 저장 후) PROCESS_CREATED 이벤트 발행 (AFTER_COMMIT로 history 저장)

        return saved.getId();
    }


    // 프로세스 상세 보기
    @Transactional(readOnly = true)
    public ProcessDetailResDto getProcessDetail(Long projectId, Long processId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 조회 가능)

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));

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
                .toList();

        // ---- TODO: fieldIds / assignees / files / feedbacks 임시 ----
        List<Long> fieldIds = List.of();             // TODO: ProcessField 연동되면 채우기
        List<AssigneeResDto> assignees = List.of();  // TODO: ProcessUser + User 연동되면 채우기
        List<FileResDto> files = List.of();          // TODO: SharedDocument 메타(이름/URL/타입) 연동되면 채우기
        List<ProcessFeedbackCreateResDto> feedbacks = List.of(); // TODO: ProcessFeedback 조회 붙이면 채우기

        return new ProcessDetailResDto(
                process.getId(),
                process.getTitle(),
                process.getContent(),
                process.getStatus(),
                process.getStartAt(),
                process.getEndAt(),
                process.getStatusOrder(),
                fieldIds,
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
    public ProcessBasicUpdateResDto updateProcessBasic(Long projectId, Long processId, ProcessBasicUpdateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 수정 가능)

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));

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

        // 7) TODO: field_ids 교체
        if (req.fieldIds() != null) {
            // TODO: process.getProcessFields().clear();
            // TODO: for each fieldId -> add ProcessField
        }

        // 8) TODO : assignee_ids 교체
        if (req.assigneeIds() != null) {
            // TODO: process.getProcessUsers().clear();
            // TODO: for each userId -> add ProcessUser(role=ASSIGNEE)
        }

        // TODO(HISTORY): before/after 비교로 실제 변경이 있을 때만 PROCESS_UPDATED 이벤트 발행



        return new ProcessBasicUpdateResDto(
                process.getId(),
                process.getTitle(),
                process.getContent(),
                process.getStatus(),
                process.getStartAt(),
                process.getEndAt(),
                (req.fieldIds() != null) ? req.fieldIds() : null,         // TODO: 실제 연결되면 process에서 꺼내기
                (req.assigneeIds() != null) ? req.assigneeIds() : null,   // TODO: 실제 연결되면 process에서 꺼내기
                mentionIdsForRes,
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
    public void deleteProcess(Long projectId, Long processId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 삭제 가능)

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "processId=" + processId + ", projectId=" + projectId
                ));

        process.softDeleteCascade();

        processMentionRepository.findAllByProcessId(process.getId())
                .forEach(ProcessMention::softDelete);
        // TODO(HISTORY): softDelete()로 deletedAt 세팅 후 PROCESS_DELETED 이벤트 발행
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

        /**
         * TODO(Field 연동):
         * - ProcessField(조인 테이블) 연동 후 fieldIds를 채운다.
         * - 조회 성능을 위해 week 조회 쿼리에 processFields/field를 fetch join 하거나
         *   processIds IN 으로 field 매핑을 한번에 조회하여 Map으로 합친다.
         * - fieldIds가 비어있으면 "Team(공통)" 레인으로 분류된다.
         */
        List<Long> fieldIds = List.of(); // 임시

        /**
         * TODO(Assignee/User 연동):
         * - ProcessUser + User 연동 후 assignees(프로필/이름 등)를 채운다.
         * - 필요 시 ProcessUserRole(ASSIGNEE 등) 기준으로 필터링한다.
         */
        List<AssigneeResDto> assignees = List.of(); // 임시

        return new ProcessCardResDto(
                p.getId(),
                p.getStatus(),
                p.getTitle(),
                done,
                whole,
                p.getStartAt(),
                p.getEndAt(),
                leftDay,
                fieldIds,
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

        /**
         * 레인 분리 정책:
         * - fieldIds empty: Team(공통) 위크미션 Task 레인
         * - fieldIds not empty: 파트별 레인(Design/Backend/Frontend 등)
         *
         * TODO(Field 연동):
         * - fieldIds가 실제로 채워지면 아래 분리가 정상적으로 동작한다.
         */

        List<ProcessCardResDto> commonLane = weekCards.stream()
                .filter(c -> c.fieldIds() == null || c.fieldIds().isEmpty())
                .toList();

        Map<Long, List<ProcessCardResDto>> byFieldMap = weekCards.stream()
                .filter(c -> c.fieldIds() != null && !c.fieldIds().isEmpty())
                .flatMap(c -> c.fieldIds().stream().map(fid -> new AbstractMap.SimpleEntry<>(fid, c)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        // TODO(FieldRepository 연동):
        // - fieldName, fieldOrder를 실제 Field 데이터로 세팅

        List<FieldGroupResDto> byField = byFieldMap.entrySet().stream()
                .map(e -> new FieldGroupResDto(
                        e.getKey(),
                        null,
                        Integer.MAX_VALUE,
                        e.getValue()
                ))
                .sorted(Comparator.comparingInt(FieldGroupResDto::fieldOrder))
                .toList();

        return new ProcessWeekResDto(weekStart, commonLane, byField);
    }

    // 주차별 프로세스 조회 서비스
    @Transactional(readOnly = true)
    public ProcessWeekResDto getWeekProcesses(Long projectId, LocalDate startDate) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 조회 가능)

        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROJECT_NOT_FOUND,
                        "projectId = " + projectId
                ));


        LocalDate weekStart = normalizeWeekStart(startDate);
        LocalDate weekEnd = weekStart.plusDays(6);

        // TODO(Field 연동 전): 현재는 기간 기반으로 프로세스만 조회
        // Field 연동 후에는 카드 응답(fieldIds)에 따라 아래 레인 분리가 정상 동작함.
        // TODO(성능/N+1):Field/User/TaskItem 연동 완료 후, week 조회 시 연관 컬렉션 접근으로 N+1이 발생할 수 있음. 최적화 시키기
        // - ProcessRepository의 week 조회(findAllInRangeOrdered)를 fetch join 또는 @EntityGraph로 전환해서
        //   필요한 연관(taskItems, processFields->field, processUsers->user 등)을 한번에 로딩하도록 최적화한다.
        List<Process> processes = processRepository.findAllInRangeOrdered(projectId, weekStart, weekEnd);

        List<ProcessCardResDto> cards = processes.stream()
                .map(this::toProcessCardResDTO)
                .toList();

        return buildWeekDto(weekStart, cards);

    }



    private ProcessStatusGroupResDto toGroup(ProcessStatus status, List<ProcessCardResDto> cards) {
        List<ProcessCardResDto> filtered = cards.stream()
                .filter(c -> c.processStatus() == status)
                .toList();

        return new ProcessStatusGroupResDto(
                status,
                filtered.size(),
                filtered
        );
    }

    // 파트별 프로세스 조회 서비스
    @Transactional(readOnly = true)
    public ProcessPartResDto getPartProcesses(Long projectId, Long fieldId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 조회 가능)

        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROJECT_NOT_FOUND,
                        "projectId = " + projectId
                ));

        /** TODO :
         * - Team 탭(fieldId == null): 모든 프로세스(필드/파트 무관) 조회
         * - Part 탭(fieldId != null): 해당 fieldId에 매핑된 프로세스만 조회
         */
        List<Process> processes = (fieldId == null)
                ? processRepository.findAllForTeamBoard(projectId)     // 전체
                : processRepository.findAllForPartBoard(projectId, fieldId); // field 필터


        // TODO(성능/N+1):
        // - Field/User/TaskItem 연동 완료 후, toProcessCardResDTO에서 연관 컬렉션 접근으로 N+1 발생 가능
        // - 레포지토리 쿼리를 fetch join 또는 @EntityGraph로 전환하여 필요한 연관을 한번에 로딩하도록 개선
        List<ProcessCardResDto> cards = processes.stream()
                .map(this::toProcessCardResDTO)
                .toList();

        // 상태별 그룹핑 (UI 컬럼 순서 고정)
        List<ProcessStatusGroupResDto> groups = List.of(
                toGroup(ProcessStatus.PLANNING, cards),
                toGroup(ProcessStatus.IN_PROGRESS, cards),
                toGroup(ProcessStatus.DONE, cards),
                toGroup(ProcessStatus.BACKLOG, cards)
        );

        return new ProcessPartResDto(fieldId, groups);
    }


    // 프로세스 위치 상태 정렬 변경 서비스
    @Transactional
    public ProcessOrderUpdateResDto updateProcessOrder(Long projectId, Long processId, ProcessOrderUpdateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 순서/상태 변경 가능)
        // TODO(인가-추가): fieldId가 붙으면 "같은 field 레인 권한" 규칙도 여기서 확정

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));

        // 변경 전 스냅샷
        ProcessStatus beforeStatus = process.getStatus();
        Integer beforeOrder = process.getStatusOrder();
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
            if (new java.util.HashSet<>(orderedIds).size() != orderedIds.size()) {
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

            // TODO(Field 연동 후):
            // - ProcessField 구조 확정 시 아래 검증 추가
            //   Team 레인(공통) : field 매핑 없음
            //   Part 레인 : fieldId 동일
            // - req에 fieldId가 들어오면 targets가 전부 동일 fieldId인지 검증

            // 전체 포함 정책: 해당 레인의 전체 개수와 orderedIds 개수가 같아야 함
            // TODO(Field 연동 후): laneTotal은 (status + fieldId/Team) 기준으로 count 해야 함
            int laneTotal = processRepository.countByProjectIdAndDeletedAtIsNullAndStatus(projectId, laneStatus);
            if (laneTotal != orderedIds.size()) {
                throw new ProcessException(
                        ProcessErrorCode.INVALID_REQUEST,
                        "ordered_process_ids must include all processes in the lane(status=" + laneStatus + ")"
                );
            }

            // 요청 순서대로 statusOrder 재부여
            Map<Long, Process> map = targets.stream()
                    .collect(Collectors.toMap(Process::getId, p -> p));

            int order = 0;
            for (Long id : orderedIds) {
                map.get(id).updateStatusOrder(order++);
            }

            process = map.get(processId);
        }

        // 변경 후 스냅샷
        ProcessStatus afterStatus = process.getStatus();
        Integer afterOrder = process.getStatusOrder();
        LocalDate afterStart = process.getStartAt();
        LocalDate afterEnd = process.getEndAt();

        boolean changed =
                (beforeStatus != afterStatus) ||
                        !Objects.equals(beforeOrder, afterOrder) ||
                        !Objects.equals(beforeStart, afterStart) ||
                        !Objects.equals(beforeEnd, afterEnd);

        // TODO(HISTORY): before/after 비교로 실제 변경이 있을 때만 PROCESS_REORDERED 이벤트 발행
        // - actorUserId(로그인 유저) 필요
        // - meta 예시: before/after + orderedIds

        return new ProcessOrderUpdateResDto(
                process.getId(),
                process.getStatus(),
                process.getStatusOrder(),
                process.getStartAt(),
                process.getEndAt()
        );
    }

    // 프로세스 작업 상태 변경
    @Transactional
    public ProcessStatusUpdateResDto updateProcessStatus(Long projectId, Long processId, ProcessStatusUpdateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 상태 변경 가능)

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

        // TODO(HISTORY): before/after 비교로 실제 변경이 있을 때만 PROCESS_STATUS_CHANGED 이벤트 발행
        // - actorUserId(로그인 유저) 필요
        // - meta 예시: beforeStatus / afterStatus

        return new ProcessStatusUpdateResDto(
                process.getId(),
                process.getStatus(),
                process.getUpdatedAt()
        );
    }

}
