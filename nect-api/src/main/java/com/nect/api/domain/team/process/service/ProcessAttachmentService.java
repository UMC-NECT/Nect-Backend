package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.file.enums.FileErrorCode;
import com.nect.api.domain.team.file.exception.FileException;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessLinkCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.enums.AttachmentErrorCode;
import com.nect.api.domain.team.process.exception.AttachmentException;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessSharedDocument;
import com.nect.core.entity.team.process.enums.ProcessType;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.team.process.ProcessSharedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessAttachmentService {

    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final ProcessSharedDocumentRepository processSharedDocumentRepository;


    private final ProjectHistoryPublisher historyPublisher;

    // 헬퍼 메서드
    private Process getActiveProcess(Long projectId, Long processId) {
        return processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.PROCESS_NOT_FOUND,
                        "processId=" + processId + ", projectId=" + projectId
                ));
    }

    private SharedDocument getActiveDocument(Long projectId, Long fileId) {
        return sharedDocumentRepository.findByIdAndProjectIdAndDeletedAtIsNull(fileId, projectId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.FILE_NOT_FOUND,
                        "projectId=" + projectId + ", fileId=" + fileId
                ));
    }

    private void validateFileAttachReq(ProcessFileAttachReqDto req) {
        if (req == null || req.fileId() == null) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_REQUEST, "file_id is required");
        }
    }

    private void validateLinkCreateReq(ProcessLinkCreateReqDto req) {
        if (req == null || req.linkUrl() == null || req.linkUrl().isBlank()) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_REQUEST, "url is required");
        }

        if (req.title() == null || req.title().isBlank()) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_REQUEST, "title is required");
        }
    }

    private void assertWeekMissionLeader(Long projectId, Long userId) {
        boolean ok = projectUserRepository.existsByProjectIdAndUserIdAndMemberTypeAndMemberStatus(
                projectId, userId, ProjectMemberType.LEADER, ProjectMemberStatus.ACTIVE
        );
        if (!ok) {
            throw new AttachmentException(AttachmentErrorCode.FORBIDDEN,
                    "WEEK_MISSION은 프로젝트 리더만 수정할 수 있습니다. projectId=" + projectId + ", userId=" + userId);
        }
    }

    private void assertAttachmentPermission(Long projectId, Long userId, Process process) {
        if (process.getProcessType() == ProcessType.WEEK_MISSION) {
            assertWeekMissionLeader(projectId, userId);
            return;
        }

        if (!projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(projectId, userId, ProjectMemberStatus.ACTIVE)) {
            throw new AttachmentException(AttachmentErrorCode.FORBIDDEN,
                    "not an active project member. projectId=" + projectId + ", userId=" + userId);
        }
    }

    // 프로세스 파일 첨부 서비스
    @Transactional
    public ProcessFileAttachResDto attachFile(Long projectId, Long userId, Long processId, ProcessFileAttachReqDto req) {
        validateFileAttachReq(req);

        Process process = getActiveProcess(projectId, processId);
        assertAttachmentPermission(projectId, userId, process);

        SharedDocument doc = getActiveDocument(projectId, req.fileId());

        if (doc.getDocumentType() != DocumentType.FILE) {
            throw new AttachmentException(
                    AttachmentErrorCode.INVALID_REQUEST,
                    "file attach API only allows FILE documents. documentId=" + doc.getId()
            );
        }

        if (processSharedDocumentRepository.existsByProcessIdAndDocumentIdAndDeletedAtIsNull(process.getId(), doc.getId())) {
            throw new AttachmentException(
                    AttachmentErrorCode.FILE_ALREADY_ATTACHED,
                    "processId=" + processId + ", fileId=" + doc.getId()
            );
        }

        ProcessSharedDocument psd = ProcessSharedDocument.builder()
                .process(process)
                .document(doc)
                .attachedAt(LocalDateTime.now())
                .build();

        processSharedDocumentRepository.save(psd);


        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("fileId", doc.getId());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_ATTACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        return new ProcessFileAttachResDto(doc.getId());
    }

    // 프로세스 파일 첨부해제 서비스
    @Transactional
    public void detachFile(Long projectId, Long userId, Long processId, Long fileId) {
        Process process = getActiveProcess(projectId, processId);
        assertAttachmentPermission(projectId, userId, process);

        ProcessSharedDocument psd = processSharedDocumentRepository
                .findByProcessIdAndDocumentIdAndDeletedAtIsNull(process.getId(), fileId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.FILE_NOT_ATTACHED,
                        "processId=" + processId + ", fileId=" + fileId
                ));

        psd.softDelete();

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("fileId", fileId);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_DETACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

    }

    // 프로세스 링크 추가 서비스
    @Transactional
    public ProcessFileAttachResDto createLink(Long projectId, Long userId, Long processId, ProcessLinkCreateReqDto req) {
        return createAndAttachLink(projectId, userId, processId, req);
    }

    // 프로세스 링크 삭제 서비스
    @Transactional
    public void deleteLink(Long projectId, Long userId, Long processId, Long linkId) {
        Process process = getActiveProcess(projectId, processId);
        assertAttachmentPermission(projectId, userId, process);

        ProcessSharedDocument psd = processSharedDocumentRepository
                .findByProcessIdAndDocumentIdAndDeletedAtIsNull(process.getId(), linkId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.LINK_NOT_ATTACHED,
                        "processId=" + processId + ", documentId=" + linkId
                ));


        psd.softDelete();

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("documentId", linkId);
        meta.put("type", "LINK");

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_DETACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

    }

    // 프로세스 링크 추가
    @Transactional
    public ProcessFileAttachResDto createAndAttachLink(Long projectId, Long userId, Long processId, ProcessLinkCreateReqDto req) {
        validateLinkCreateReq(req);

        Process process = getActiveProcess(projectId, processId);
        assertAttachmentPermission(projectId, userId, process);


        User user = projectUserRepository.findActiveUserByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new FileException(FileErrorCode.FORBIDDEN, "not active member"));

        SharedDocument doc = SharedDocument.ofLink(user, process.getProject(), req.title().trim(), req.linkUrl().trim());
        SharedDocument saved = sharedDocumentRepository.save(doc);

        if (processSharedDocumentRepository.existsByProcessIdAndDocumentIdAndDeletedAtIsNull(process.getId(), saved.getId())) {
            throw new AttachmentException(AttachmentErrorCode.FILE_ALREADY_ATTACHED, "processId=" + processId + ", documentId=" + saved.getId());
        }

        ProcessSharedDocument psd = ProcessSharedDocument.builder()
                .process(process)
                .document(saved)
                .attachedAt(LocalDateTime.now())
                .build();

        processSharedDocumentRepository.save(psd);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processId", processId);
        meta.put("documentId", saved.getId());
        meta.put("type", "LINK");
        meta.put("url", saved.getLinkUrl());
        meta.put("title", saved.getTitle());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_ATTACHED,
                HistoryTargetType.PROCESS,
                processId,
                meta
        );

        return new ProcessFileAttachResDto(saved.getId());
    }
}
