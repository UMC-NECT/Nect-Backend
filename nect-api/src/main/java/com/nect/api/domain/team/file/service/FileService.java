package com.nect.api.domain.team.file.service;

import com.nect.api.domain.team.file.dto.res.FileDownloadUrlResDto;
import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.enums.FileErrorCode;
import com.nect.api.domain.team.file.exception.FileException;
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
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private static final long MB = 1024L * 1024L;

    private static final long MAX_5MB = 5L * MB;
    private static final long MAX_20MB = 20L * MB;

    private static final Set<FileExt> LIMIT_5MB = EnumSet.of(FileExt.JPG, FileExt.PNG, FileExt.SVG);
    private static final Set<FileExt> LIMIT_20MB = EnumSet.of(FileExt.PDF, FileExt.DOCS, FileExt.PPTX, FileExt.FIG, FileExt.ZIP);

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

        if (file == null) {
            throw new FileException(FileErrorCode.INVALID_REQUEST, "file is null");
        }

        if (file.isEmpty()) {
            throw new FileException(FileErrorCode.EMPTY_FILE, "file is empty");
        }

        long fileSize = file.getSize();

        String originalName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "file"
                : file.getOriginalFilename();

        FileExt ext = resolveExtOrThrow(originalName);

        validateSizeOrThrow(ext, fileSize);

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

    private void validateSizeOrThrow(FileExt ext, long fileSize) {
        long max;

        if (LIMIT_5MB.contains(ext)) {
            max = MAX_5MB;
        } else if (LIMIT_20MB.contains(ext)) {
            max = MAX_20MB;
        } else {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_EXT, "fileExt = " + ext);
        }

        if (fileSize > max) {
            throw new FileException(
                    FileErrorCode.FILE_SIZE_EXCEEDED,
                    "fileExt = " + ext + ", size = " + fileSize + ", max = " + max
            );
        }
    }

    private FileExt resolveExtOrThrow(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        String ext = (dot >= 0) ? lower.substring(dot + 1) : "";

        return switch (ext) {
            case "jpg", "jpeg" -> FileExt.JPG;
            case "png" -> FileExt.PNG;
            case "svg" -> FileExt.SVG;
            case "pdf" -> FileExt.PDF;
            case "docs" -> FileExt.DOCS;
            case "pptx" -> FileExt.PPTX;
            case "fig" -> FileExt.FIG;
            case "zip" -> FileExt.ZIP;
            default -> throw new FileException(FileErrorCode.UNSUPPORTED_FILE_EXT, "fileName=" + fileName);
        };
    }
}
