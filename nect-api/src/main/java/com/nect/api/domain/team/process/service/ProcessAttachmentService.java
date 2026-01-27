package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessLinkCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.dto.res.ProcessLinkCreateResDto;
import com.nect.api.domain.team.process.enums.AttachmentErrorCode;
import com.nect.api.domain.team.process.exception.AttachmentException;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.process.Link;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessSharedDocument;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.process.LinkRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.team.process.ProcessSharedDocumentRepository;
import com.nimbusds.openid.connect.sdk.assurance.evidences.attachment.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessAttachmentService {

    private final ProcessRepository processRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final ProcessSharedDocumentRepository processSharedDocumentRepository;
    private final LinkRepository linkRepository;

    // TODO(TEAM EVENT FACADE): Attachment 변경 시(Notification + History) ActivityFacade로 통합 예정

    // TODO(인증/인가): Security/User 붙이면 CurrentUserProvider(또는 AuthFacade), ProjectUserRepository(멤버십 검증용) 주입 예정

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

        if (processSharedDocumentRepository.existsByProcessIdAndDocumentId(process.getId(), doc.getId())) {
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
        // TODO(HISTORY): FILE_ATTACHED 이벤트 발행(ProjectHistoryEvent, targetType=PROCESS or PROCESS_ATTACHMENT, targetId=processId, metaJson에 fileId 포함)

        return new ProcessFileAttachResDto(doc.getId());
    }

    // 프로세스 파일 첨부해제 서비스
    @Transactional
    public void detachFile(Long projectId, Long processId, Long fileId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증

        Process process = getActiveProcess(projectId, processId);

        ProcessSharedDocument psd = processSharedDocumentRepository
                .findByProcessIdAndDocumentId(process.getId(), fileId)
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.FILE_NOT_ATTACHED,
                        "processId=" + processId + ", fileId=" + fileId
                ));

        processSharedDocumentRepository.delete(psd);

        // TODO(Notification): 파일 첨부해제 알림 트리거(AFER_COMMIT 권장)
        // TODO(HISTORY): FILE_DETACHED 이벤트 발행(metaJson: {processId, fileId})

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
        // TODO(HISTORY): LINK_CREATED 이벤트 발행(targetId=saved.getId() 또는 processId, metaJson에 url 포함)

        return new ProcessLinkCreateResDto(saved.getId());
    }

    // 프로세스 링크 삭제 서비스
    @Transactional
    public void deleteLink(Long projectId, Long processId, Long linkId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증

        Process process = getActiveProcess(projectId, processId);

        Link link = linkRepository.findByIdAndProcessId(linkId, process.getId())
                .orElseThrow(() -> new AttachmentException(
                        AttachmentErrorCode.LINK_NOT_FOUND,
                        "linkId=" + linkId + ", processId=" + processId
                ));

        // TODO(HISTORY/NOTI): 삭제 전 스냅샷 필요하면 여기서 url 저장(삭제 후엔 link 엔티티 없음)
        linkRepository.delete(link);

        // TODO(Notification): 링크 삭제 알림 트리거(AFER_COMMIT 권장)
        // TODO(HISTORY): LINK_DELETED 이벤트 발행(metaJson: {processId, linkId, url(optional)})

    }
}
