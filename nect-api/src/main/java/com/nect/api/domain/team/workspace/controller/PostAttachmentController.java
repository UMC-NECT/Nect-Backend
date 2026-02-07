package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.dto.req.PostLinkCreateReqDto;
import com.nect.api.domain.team.workspace.dto.res.PostAttachmentResDto;
import com.nect.api.domain.team.workspace.facade.PostAttachmentFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/boards/posts/{postId}/attachments")
public class PostAttachmentController {

    private final PostAttachmentFacade postAttachmentFacade;

    // 파일 업로드 + 첨부
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostAttachmentResDto> uploadAndAttachFile(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("file") MultipartFile file
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(postAttachmentFacade.uploadAndAttachFile(projectId, userId, postId, file));
    }

    // 링크 생성 + 첨부
    @PostMapping("/links")
    public ApiResponse<PostAttachmentResDto> createAndAttachLink(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PostLinkCreateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(postAttachmentFacade.createAndAttachLink(projectId, userId, postId, req));
    }

    // 첨부 해제 (파일/링크 공통)
    @DeleteMapping("/{documentId}")
    public ApiResponse<Void> detach(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        postAttachmentFacade.detach(projectId, userId, postId, documentId);
        return ApiResponse.ok(null);
    }
}
