package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.workspace.dto.req.SharedDocumentNameUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentNameUpdateResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsGetResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.enums.SharedDocumentsSort;
import com.nect.api.domain.team.workspace.service.BoardsSharedDocumentService;
import com.nect.core.entity.team.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardsSharedDocumentFacade {

    private final BoardsSharedDocumentService service;

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

}