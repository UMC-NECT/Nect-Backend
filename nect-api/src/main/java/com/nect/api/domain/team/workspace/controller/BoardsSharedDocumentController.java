package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.dto.req.SharedDocumentNameUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentNameUpdateResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsGetResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.enums.SharedDocumentsSort;
import com.nect.api.domain.team.workspace.facade.BoardsSharedDocumentFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.team.enums.DocumentType;
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


    // 공유 문서함 조회
    @GetMapping("/shared-documents")
    public ApiResponse<SharedDocumentsGetResDto> getSharedDocuments(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) DocumentType type,
            @RequestParam(defaultValue = "RECENT") SharedDocumentsSort sort
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(facade.getDocuments(projectId, userId, page, size, type, sort));
    }


    // 문서 이름 수정
    @PatchMapping("/shared-documents/{documentId}/name")
    public ApiResponse<SharedDocumentNameUpdateResDto> rename(
            @PathVariable Long projectId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody SharedDocumentNameUpdateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(facade.rename(projectId, userId, documentId, req));
    }

    // 문서 삭제
    @DeleteMapping("/shared-documents/{documentId}")
    public ApiResponse<Void> deleteSharedDocument(
            @PathVariable Long projectId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        facade.delete(projectId, userId, documentId);
        return ApiResponse.ok(null);
    }
}
