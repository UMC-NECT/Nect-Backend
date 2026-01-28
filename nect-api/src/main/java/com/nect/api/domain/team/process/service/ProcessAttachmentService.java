package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessLinkCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.dto.res.ProcessLinkCreateResDto;
import com.nect.api.domain.team.process.enums.AttachmentErrorCode;
import com.nect.api.domain.team.process.exception.AttachmentException;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.process.Link;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessSharedDocument;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.process.LinkRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.team.process.ProcessSharedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessAttachmentService {

    private final ProcessRepository processRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final ProcessSharedDocumentRepository processSharedDocumentRepository;
    private final LinkRepository linkRepository;


    // TODO(TEAM EVENT FACADE): Attachment 변경 시(Notification) ActivityFacade로 통합 예정

    // TODO(인증/인가): Security/User 붙이면 CurrentUserProvider(또는 AuthFacade), ProjectUserRepository(멤버십 검증용) 주입 예정

    private final ProjectHistoryPublisher historyPublisher;

    private Process getActiveProcess(Long projectId, Long processId) {
        return processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.PROCESS_NOT_FOUND,
                        "processId=" + processId + ", projectId=" + projectId
                ));
    }

    private SharedDocument getActiveDocument(Long fileId /*, Long projectId */) {
        return sharedDocumentRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.FILE_NOT_FOUND,
                        "fileId=" + fileId
                ));
    }

    private void validateFileAttachReq(ProcessFileAttachReqDto req) {
        if (req == null || req.fileId() == null) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_REQUEST, "file_id is required");
        }
    }

    private void validateLinkCreateReq(ProcessLinkCreateReqDto req) {
        if (req == null || req.url() == null || req.url().isBlank()) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_REQUEST, "url is required");
        }
    }

    // 프로세스 파일 첨부 서비스
    @Transactional
    public ProcessFileAttachResDto attachFile(Long projectId, Long processId, ProcessFileAttachReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 파일 첨부 가능)

        validateFileAttachReq(req);

        Process process = getActiveProcess(projectId, processId);

        SharedDocument doc = getActiveDocument(req.fileId());

        if (processSharedDocumentRepository.existsByProcessIdAndDocumentIdAndDeletedAtIsNull(process.getId(), doc.getId())) {
            throw new AttachmentException(
                    AttachmentErrorCode.FILE_ALREADY_ATTACHED,
                    "processId=" + processId + ", fileId=" + doc.getId()
            );
        }

        ProcessSharedDocument psd = ProcessSharedDocument.builder()
                .process(process)
                .document(doc)
                .attachedAt(null)
                .build();

        processSharedDocumentRepository.save(psd);

        // TODO(Notification): 파일 첨부 알림 트리거(수신자=프로젝트 멤버/프로세스 관련자, AFTER_COMMIT 전환 권장)


        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("fileId", doc.getId());

        historyPublisher.publish(
                projectId,
                HistoryAction.DOCUMENT_ATTACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        return new ProcessFileAttachResDto(doc.getId());
    }

    // 프로세스 파일 첨부해제 서비스
    @Transactional
    public void detachFile(Long projectId, Long processId, Long fileId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증

        Process process = getActiveProcess(projectId, processId);

        ProcessSharedDocument psd = processSharedDocumentRepository
                .findByProcessIdAndDocumentIdAndDeletedAtIsNull(process.getId(), fileId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.FILE_NOT_ATTACHED,
                        "processId=" + processId + ", fileId=" + fileId
                ));

        psd.softDelete();

        // TODO(Notification): 파일 첨부해제 알림 트리거(AFER_COMMIT 권장)
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("fileId", fileId);

        historyPublisher.publish(
                projectId,
                HistoryAction.DOCUMENT_DETACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

    }

    // 프로세스 링크 추가 서비스
    @Transactional
    public ProcessLinkCreateResDto createLink(Long projectId, Long processId, ProcessLinkCreateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 링크 추가 가능)

        validateLinkCreateReq(req);

        Process process = getActiveProcess(projectId, processId);

        Link link = Link.builder()
                .process(process)
                .url(req.url().trim())
                .build();


        Link saved = linkRepository.save(link);

        // TODO(Notification): 링크 추가 알림 트리거(AFER_COMMIT 권장)

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("linkId", saved.getId());
        meta.put("url", saved.getUrl());

        historyPublisher.publish(
                projectId,
                HistoryAction.LINK_ATTACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        return new ProcessLinkCreateResDto(saved.getId());
    }

    // 프로세스 링크 삭제 서비스
    @Transactional
    public void deleteLink(Long projectId, Long processId, Long linkId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증

        Process process = getActiveProcess(projectId, processId);

        Link link = linkRepository.findByIdAndProcessIdAndDeletedAtIsNull(linkId, process.getId())
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.LINK_NOT_FOUND,
                        "linkId=" + linkId + ", processId=" + processId
                ));

        String beforeUrl = link.getUrl();
        link.softDelete();

        // TODO(Notification): 링크 삭제 알림 트리거(AFER_COMMIT 권장)

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("linkId", linkId);
        meta.put("url", beforeUrl);

        historyPublisher.publish(
                projectId,
                HistoryAction.LINK_DETACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

    }
}
