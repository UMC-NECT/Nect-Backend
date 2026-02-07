package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardsSharedDocumentService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final S3Service s3Service;

    private String toPresignedUserImage(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) return null;
        return s3Service.getPresignedGetUrl(fileKey);
    }

    /**
     * 공유 문서함 프리뷰 조회
     * - 팀보드 가운데 "공유 문서함" 카드에 보여줄 목록 (기본 4개)
     * - 핀 우선 + 최신순
     */
    @Transactional(readOnly = true)
    public SharedDocumentsPreviewResDto getPreview(Long projectId, Long userId, int limit) {

        // 프로젝트 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // 멤버 검증
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new BoardsException(
                    BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId
            );
        }

        int safeLimit = Math.max(1, Math.min(limit, 20));

        List<SharedDocument> docs =
                sharedDocumentRepository.findPreviewByProjectId(projectId, PageRequest.of(0, safeLimit));

        List<SharedDocumentsPreviewResDto.DocumentDto> result = docs.stream().map(d -> {
            User u = d.getCreatedBy();

            String profileUrl = (u == null) ? null : toPresignedUserImage(u.getProfileImageName());

            return new SharedDocumentsPreviewResDto.DocumentDto(
                    d.getId(),
                    d.isPinned(),
                    d.getTitle(),
                    d.getFileName(),
                    d.getFileExt(),
                    d.getFileUrl(),
                    d.getFileSize(),
                    d.getCreatedAt(),
                    new SharedDocumentsPreviewResDto.UploaderDto(
                            u.getUserId(),
                            u.getName(),
                            u.getNickname(),
                            profileUrl
                    )
            );
        }).toList();

        return new SharedDocumentsPreviewResDto(result);
    }
}