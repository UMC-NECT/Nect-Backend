package com.nect.api.domain.team.file.service;

import com.nect.api.domain.team.file.dto.res.FileDownloadUrlResDto;
import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.enums.FileErrorCode;
import com.nect.api.domain.team.file.exception.FileException;
import com.nect.api.domain.team.file.util.FileUploadValidator;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    private void assertActiveProjectMember(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new FileException(
                    FileErrorCode.FORBIDDEN,
                    "not an active project member. projectId=" + projectId + ", userId=" + userId
            );
        }
    }

    // 다운로드 서비스
    @Transactional(readOnly = true)
    public FileDownloadUrlResDto getDownloadUrl(Long projectId, Long userId, Long fileId) {
        assertActiveProjectMember(projectId, userId);

        SharedDocument doc = sharedDocumentRepository.findByIdAndProjectIdAndDeletedAtIsNull(fileId, projectId)
                .orElseThrow(() -> new FileException(
                        FileErrorCode.FILE_NOT_FOUND,
                        "fileId=" + fileId + ", projectId=" + projectId
                ));

        String fileKey = doc.getFileUrl();
        String presignedUrl = s3Service.getPresignedGetUrl(fileKey);

        return new FileDownloadUrlResDto(
                doc.getId(),
                doc.getFileName(),
                doc.getFileExt(),
                doc.getFileSize(),
                presignedUrl
        );
    }

    // 업로드 서비스
    public FileUploadResDto upload(Long projectId, Long userId, MultipartFile file) {
        assertActiveProjectMember(projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new FileException(FileErrorCode.PROJECT_NOT_FOUND, "projectId = " + projectId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FileException(FileErrorCode.USER_NOT_FOUND, "userId=" + userId));

        FileUploadValidator.validateNotEmpty(file);

        long fileSize = file.getSize();

        String originalName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "file"
                : file.getOriginalFilename();

        FileExt ext = FileUploadValidator.resolveExtOrThrow(originalName);

        FileUploadValidator.validateSizeOrThrow(ext, fileSize);

        // R2 업로드
        String fileKey;
        try {
            fileKey = s3Service.uploadFile(file);
        } catch (IOException e) {
            throw new FileException(FileErrorCode.FILE_UPLOAD_FAILED, "R2 upload failed", e);
        }

        SharedDocument doc = SharedDocument.builder()
                .createdBy(user)
                .project(project)
                .isPinned(false)
                .title(originalName)
                .description(null)
                .fileName(originalName)
                .fileExt(ext)
                .fileUrl(fileKey)
                .fileSize(fileSize)
                .build();

        SharedDocument saved = sharedDocumentRepository.save(doc);

        String downloadUrl = s3Service.getPresignedGetUrl(saved.getFileUrl());

        return new FileUploadResDto(
                saved.getId(),
                saved.getFileName(),
                saved.getFileUrl(),
                saved.getFileExt(),
                fileSize,
                downloadUrl
        );
    }

}
