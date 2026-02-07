package com.nect.api.domain.team.process.facade;

import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessLinkCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileUploadAndAttachResDto;
import com.nect.api.domain.team.process.dto.res.ProcessLinkCreateAndAttachResDto;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.api.domain.team.process.service.ProcessAttachmentService;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.enums.ProcessType;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class ProcessAttachmentFacade {
    private final FileService fileService;
    private final ProcessAttachmentService processAttachmentService;

    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;

    /**
     * 프로세스 모달에서 "파일 업로드" 시:
     * 1) SharedDocument 생성(=공유문서 저장)
     * 2) 해당 프로세스에 첨부(=ProcessSharedDocument 생성)
     */
    @Transactional
    public ProcessFileUploadAndAttachResDto uploadAndAttachFile(Long projectId, Long userId, Long processId, MultipartFile file) {
        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.PROCESS_NOT_FOUND, "processId=" + processId));

        // 프로세스 타입이 위크미션이면 업로드 전에 리더 체크
        if (process.getProcessType() == ProcessType.WEEK_MISSION) {
            boolean isLeader = projectUserRepository.existsByProjectIdAndUserIdAndMemberTypeAndMemberStatus(
                    projectId, userId, ProjectMemberType.LEADER, ProjectMemberStatus.ACTIVE
            );
            if (!isLeader) throw new ProcessException(ProcessErrorCode.FORBIDDEN, "WEEK_MISSION은 리더만 업로드/첨부 가능");
        } else {
            // 일반 프로세스면 ACTIVE 멤버 체크
            if (!projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(projectId, userId, ProjectMemberStatus.ACTIVE)) {
                throw new ProcessException(ProcessErrorCode.FORBIDDEN, "not active member");
            }
        }

        // 파일 업로드 -> 첨부
        FileUploadResDto uploaded = fileService.upload(projectId, userId, file);

        ProcessFileAttachResDto attached = processAttachmentService.attachFile(
                projectId,
                userId,
                processId,
                new ProcessFileAttachReqDto(uploaded.fileId())
        );

        return new ProcessFileUploadAndAttachResDto(
                attached.fileId(),
                uploaded.fileName(),
                uploaded.fileUrl(),
                uploaded.fileType(),
                uploaded.fileSize()
        );
    }

    // 링크 첨부 + 공유 문서 저장 서비스
    @Transactional
    public ProcessLinkCreateAndAttachResDto createAndAttachLink(Long projectId, Long userId, Long processId, ProcessLinkCreateReqDto req) {
        Process process = processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(ProcessErrorCode.PROCESS_NOT_FOUND, "processId=" + processId));

        // 권한 체크 (uploadAndAttachFile과 동일)
        if (process.getProcessType() == ProcessType.WEEK_MISSION) {
            boolean isLeader = projectUserRepository.existsByProjectIdAndUserIdAndMemberTypeAndMemberStatus(
                    projectId, userId, ProjectMemberType.LEADER, ProjectMemberStatus.ACTIVE
            );
            if (!isLeader) throw new ProcessException(ProcessErrorCode.FORBIDDEN, "WEEK_MISSION은 리더만 링크 추가 가능");
        } else {
            if (!projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(projectId, userId, ProjectMemberStatus.ACTIVE)) {
                throw new ProcessException(ProcessErrorCode.FORBIDDEN, "not active member");
            }
        }

        // 서비스에서 SharedDocument(LINK) 생성 + attach 수행
        ProcessFileAttachResDto attached = processAttachmentService.createAndAttachLink(projectId, userId, processId, req);

        // 응답은 UI 필요에 따라 title/url 포함해서 내려도 됨
        return new ProcessLinkCreateAndAttachResDto(attached.fileId(), req.title().trim(), req.linkUrl().trim());
    }


}
