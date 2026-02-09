package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.workspace.dto.req.SharedDocumentLinkCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.SharedDocumentNameUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentCreatedResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentNameUpdateResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsGetResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.enums.SharedDocumentsSort;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.api.domain.team.workspace.service.BoardsSharedDocumentService;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.user.User;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoardsSharedDocumentFacade {

    private final FileService fileService;
    private final BoardsSharedDocumentService service;
    private final ProjectHistoryPublisher projectHistoryPublisher;
    private final UserRepository userRepository;

    public SharedDocumentsPreviewResDto getPreview(Long projectId, Long userId, int limit) {
        return service.getPreview(projectId, userId, limit);
    }

    public SharedDocumentsGetResDto getDocuments(
            Long projectId, Long userId, int page, int size, DocumentType type, SharedDocumentsSort sort
    ) {
        return service.getDocuments(projectId, userId, page, size, type, sort);
    }

    public SharedDocumentNameUpdateResDto rename(
            Long projectId, Long userId, Long documentId, SharedDocumentNameUpdateReqDto req
    ) {
        return service.rename(projectId, userId, documentId, req);
    }

    public void delete(Long projectId, Long userId, Long documentId) {
        service.delete(projectId, userId, documentId);
    }


    // 공유 문서함: 파일 업로드
    @Transactional
    public SharedDocumentCreatedResDto uploadFile(Long projectId, Long userId, MultipartFile file) {
        FileUploadResDto uploaded = fileService.upload(projectId, userId, file);


        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("documentId", uploaded.fileId());
        meta.put("type", "FILE");
        meta.put("title", uploaded.fileName());
        meta.put("fileName", uploaded.fileName());
        meta.put("fileExt", uploaded.fileType());
        meta.put("fileSize", uploaded.fileSize());

        projectHistoryPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_CREATED,
                HistoryTargetType.DOCUMENT,
                uploaded.fileId(),
                meta
        );

        return new SharedDocumentCreatedResDto(
                uploaded.fileId(),
                DocumentType.FILE,
                uploaded.fileName(),
                null,
                uploaded.fileName(),
                uploaded.fileType(),
                uploaded.fileSize(),
                uploaded.downloadUrl()
        );
    }

    // 공유 문서함: 링크 생성
    @Transactional
    public SharedDocumentCreatedResDto createLink(Long projectId, Long userId, SharedDocumentLinkCreateReqDto req) {
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.USER_NOT_FOUND, "userId=" + userId));

        SharedDocument saved = service.createLink(projectId, userId, req, actor);

        return new SharedDocumentCreatedResDto(
                saved.getId(),
                saved.getDocumentType(),   // LINK
                saved.getTitle(),
                saved.getLinkUrl(),
                null,
                null,
                0L,
                null
        );
    }
}