package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.workspace.dto.req.SharedDocumentNameUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentNameUpdateResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsGetResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.enums.SharedDocumentsSort;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.process.ProcessSharedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoardsSharedDocumentService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final ProcessSharedDocumentRepository processSharedDocumentRepository;
    private final S3Service s3Service;
    private final ProjectHistoryPublisher historyPublisher;

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

    // 공유 문서함 조회
    @Transactional(readOnly = true)
    public SharedDocumentsGetResDto getDocuments(Long projectId, Long userId,
                                                 int page, int size,
                                                 DocumentType type,
                                                 SharedDocumentsSort sort) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 50));

        Sort sortSpec = buildSort(sort);
        PageRequest pr = PageRequest.of(p, s, sortSpec);

        Page<SharedDocument> docsPage = (type == null)
                ? sharedDocumentRepository.findAllActiveByProjectId(projectId, pr)
                : sharedDocumentRepository.findAllActiveByProjectIdAndType(projectId, type, pr);

        var docs = docsPage.getContent().stream().map(d -> {
            User u = d.getCreatedBy();
            String profileUrl = (u == null) ? null : toPresignedUserImage(u.getProfileImageName());
            return new SharedDocumentsGetResDto.DocumentDto(
                    d.getId(),
                    d.isPinned(),
                    d.getDocumentType(),
                    d.getTitle(),
                    d.getFileName(),
                    d.getFileExt(),
                    d.getFileUrl(),
                    d.getLinkUrl(),
                    d.getFileSize(),
                    d.getCreatedAt(),
                    new SharedDocumentsGetResDto.UploaderDto(
                            u.getUserId(),
                            u.getName(),
                            u.getNickname(),
                            profileUrl
                    )
            );
        }).toList();

        return new SharedDocumentsGetResDto(
                docsPage.getNumber(),
                docsPage.getSize(),
                docsPage.getTotalElements(),
                docsPage.getTotalPages(),
                docs
        );
    }

    private Sort buildSort(SharedDocumentsSort sort) {
        Sort pinnedFirst = Sort.by("isPinned").descending();

        return switch (sort == null ? SharedDocumentsSort.RECENT : sort) {
            case OLDEST -> pinnedFirst
                    .and(Sort.by("createdAt").ascending())
                    .and(Sort.by("id").ascending());

            case NAME -> pinnedFirst
                    .and(Sort.by("title").ascending())
                    .and(Sort.by("id").descending());

            case FORMAT -> pinnedFirst
                    .and(Sort.by("documentType").ascending()) // FILE/LINK 묶음 정렬 안정화
                    .and(Sort.by("fileExt").ascending())
                    .and(Sort.by("title").ascending())
                    .and(Sort.by("id").descending());

            default -> pinnedFirst
                    .and(Sort.by("createdAt").descending())
                    .and(Sort.by("id").descending());
        };
    }

    // 이름 변경 서비스
    @Transactional
    public SharedDocumentNameUpdateResDto rename(Long projectId, Long userId, Long documentId, SharedDocumentNameUpdateReqDto req) {

        String after = (req == null) ? null : req.resolvedTitle();
        if (after == null || after.isBlank()) {
            throw new BoardsException(BoardsErrorCode.INVALID_REQUEST, "title is required");
        }

        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        SharedDocument doc = sharedDocumentRepository.findByIdAndProjectIdAndDeletedAtIsNull(documentId, projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.DOCUMENT_NOT_FOUND, "documentId=" + documentId));

        String before = doc.getTitle();

        doc.updateTitle(after);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("documentId", doc.getId());
        meta.put("beforeTitle", before);
        meta.put("afterTitle", after);
        meta.put("documentType", doc.getDocumentType().name());
        if (doc.getDocumentType() == DocumentType.LINK) meta.put("url", doc.getLinkUrl());
        if (doc.getDocumentType() == DocumentType.FILE) meta.put("fileExt", doc.getFileExt());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_RENAMED,
                HistoryTargetType.DOCUMENT,
                doc.getId(),
                meta
        );

        return new SharedDocumentNameUpdateResDto(doc.getId(), doc.getTitle());
    }

    // 문서 삭제 서비스
    @Transactional
    public void delete(Long projectId, Long userId, Long documentId) {

        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        SharedDocument doc = sharedDocumentRepository.findByIdAndProjectIdAndDeletedAtIsNull(documentId, projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.DOCUMENT_NOT_FOUND, "documentId=" + documentId));

        // meta 만들기 (삭제 전에 정보 확보)
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("documentId", doc.getId());
        meta.put("title", doc.getTitle());
        meta.put("documentType", doc.getDocumentType().name());
        if (doc.getDocumentType() == DocumentType.LINK) meta.put("url", doc.getLinkUrl());
        if (doc.getDocumentType() == DocumentType.FILE) meta.put("fileExt", doc.getFileExt());
        meta.put("deletedAt", LocalDateTime.now().toString());

        // 문서 삭제
        doc.softDelete();

        // 문서가 삭제되면 프로세스 첨부 목록에도 안 보이게
        int detachedCount = processSharedDocumentRepository.softDeleteAllAttachments(projectId, documentId);
        meta.put("detachedFromProcesses", detachedCount);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_DELETED,
                HistoryTargetType.DOCUMENT,
                doc.getId(),
                meta
        );
    }
}