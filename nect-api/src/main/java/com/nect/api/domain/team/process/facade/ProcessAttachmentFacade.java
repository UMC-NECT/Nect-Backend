package com.nect.api.domain.team.process.facade;

import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileUploadAndAttachResDto;
import com.nect.api.domain.team.process.service.ProcessAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProcessAttachmentFacade {
    private final FileService fileService;
    private final ProcessAttachmentService processAttachmentService;

    /**
     * 프로세스 모달에서 "파일 업로드" 시:
     * 1) SharedDocument 생성(=공유문서 저장)
     * 2) 해당 프로세스에 첨부(=ProcessSharedDocument 생성)
     */
    @Transactional
    public ProcessFileUploadAndAttachResDto uploadAndAttachFile(Long projectId, Long userId, Long processId, MultipartFile file) {
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

}
