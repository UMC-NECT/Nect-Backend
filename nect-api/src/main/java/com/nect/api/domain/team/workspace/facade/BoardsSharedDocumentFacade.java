package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.service.BoardsSharedDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardsSharedDocumentFacade {

    private final BoardsSharedDocumentService service;

    public SharedDocumentsPreviewResDto getPreview(Long projectId, Long userId, int limit) {
        return service.getPreview(projectId, userId, limit);
    }
}