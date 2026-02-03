package com.nect.api.domain.team.process.facade;

import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileUploadAndAttachResDto;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.api.domain.team.process.service.ProcessAttachmentService;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProcessAttachmentFacade {
    private final FileService fileService;
    private final ProcessAttachmentService processAttachmentService;

    private final NotificationFacade notificationFacade;
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;

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

        notifyWorkspaceFileUploaded(projectId, userId, uploaded.fileId(), uploaded.fileName());

        return new ProcessFileUploadAndAttachResDto(
                attached.fileId(),
                uploaded.fileName(),
                uploaded.fileUrl(),
                uploaded.fileType(),
                uploaded.fileSize()
        );
    }

    private void notifyWorkspaceFileUploaded(Long projectId, Long actorId, Long fileId, String fileName) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROJECT_NOT_FOUND,
                        "projectId = " + projectId
                ));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.USER_NOT_FOUND,
                        "actorId = " + actorId
                ));

        // 프로젝트 멤버 전체 조회
        List<User> receivers = projectUserRepository.findAllUsersByProjectId(projectId).stream()
                .filter(u -> u != null && u.getUserId() != null)
                .filter(u -> !Objects.equals(u.getUserId(), actorId))
                .toList();

        if (receivers.isEmpty()) return;

        NotificationCommand command = new NotificationCommand(
                NotificationType.WORKSPACE_FILE_UPLOADED,
                NotificationClassification.FILE_UPlOAD,
                NotificationScope.WORKSPACE_GLOBAL,
                fileId,
                new Object[]{ actor.getName() },
                new Object[]{ fileName },
                project
        );

        notificationFacade.notify(receivers, command);
    }

}
