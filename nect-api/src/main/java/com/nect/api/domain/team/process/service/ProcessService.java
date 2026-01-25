package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.process.dto.req.ProcessBasicUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessCreateReqDto;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.process.Link;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessMention;
import com.nect.core.entity.team.process.ProcessTaskItem;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {
    private final ProjectRepository projectRepository;
    private final ProcessRepository processRepository;
    private final SharedDocumentRepository sharedDocumentRepository;

    // TODO: Field 연동 시 주입
    // private final FieldRepository fieldRepository;

    // TODO: 담당자(assignee) 연동 시 주입
    // private final UserRepository userRepository;

    // TODO(인증/인가): Security/User 붙이면 CurrentUserProvider(또는 AuthFacade), ProjectUserRepository(멤버십 검증용) 주입 예정


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

        List<Long> mentionIds = req.mentionUserIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        process.replaceMentions(mentionIds);

        // TODO(HISTORY): 생성 성공 후(Project 저장 후) PROCESS_CREATED 이벤트 발행 (AFTER_COMMIT로 history 저장)

        return processRepository.save(process).getId();
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

    // 프로세스 기본 정보 수정
    @Transactional
    public ProcessBasicUpdateResDto updateProcessBasic(Long projectId, Long processId, ProcessBasicUpdateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 수정 가능)

        log.info("PATCH req title={}, content={}, status={}, start={}, end={}, fields={}, assignees={}, mentions={}",
                req.processTitle(), req.processContent(), req.processStatus(),
                req.startDate(), req.deadLine(),
                req.fieldIds(), req.assigneeIds(), req.mentionUserIds());

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
        List<Long> mentionIdsForRes = null;

        if (req.mentionUserIds() != null) {
            mentionIdsForRes = req.mentionUserIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            process.replaceMentions(mentionIdsForRes);
        }

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
    // TODO : softDelete를 진행해서 해당 프로세스와 연관된 업무(task), 피드백, 파일 등 프로세스 하위 자식 엔티티도 deletedAt 찍어 줄 예정
    @Transactional
    public void deleteProcess(Long projectId, Long processId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 삭제 가능)

        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "processId=" + processId + ", projectId=" + projectId
                ));

        process.softDelete();

        // TODO(HISTORY): softDelete()로 deletedAt 세팅 후 PROCESS_DELETED 이벤트 발행
    }

}
