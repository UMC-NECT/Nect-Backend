package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.facade.BoardsSharedDocumentFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/boards")
public class BoardsSharedDocumentController {

    private final BoardsSharedDocumentFacade facade;

    /**
     * 공유 문서함 프리뷰 조회
     * - 팀보드 가운데 "공유 문서함" 카드에 보여줄 문서 목록 (기본 limit=4)
     */
    @GetMapping("/shared-documents/preview")
    public ApiResponse<SharedDocumentsPreviewResDto> getSharedDocumentsPreview(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "4") int limit
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(facade.getPreview(projectId, userId, limit));
    }
}
