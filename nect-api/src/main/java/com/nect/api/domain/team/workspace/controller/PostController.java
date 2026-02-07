package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.dto.req.PostCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.PostUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.*;
import com.nect.api.domain.team.workspace.enums.PostSort;
import com.nect.api.domain.team.workspace.facade.PostFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.team.workspace.enums.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/boards/posts")
public class PostController {
    private final PostFacade postFacade;

    // 게시글 생성
    @PostMapping
    public ApiResponse<PostCreateResDto> createPost(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PostCreateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(postFacade.createPost(projectId, userId, req));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostGetResDto> getPost(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok( postFacade.getPost(projectId, userId, postId));
    }

    // 게시글 목록 조회
    @GetMapping
    public ApiResponse<PostListResDto> getPostList(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) PostType type,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long userId = userDetails.getUserId();
        int fixedSize = 10;
        return ApiResponse.ok(postFacade.getPostList(projectId, userId, type, page, fixedSize));
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ApiResponse<PostUpdateResDto> updatePost(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PostUpdateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(postFacade.updatePost(projectId, userId, postId, req));
    }

    // 게시글 좋아요
    @PostMapping("/{postId}/likes")
    public ApiResponse<PostLikeToggleResDto> togglePostLike(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(postFacade.togglePostLike(projectId, userId, postId));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        postFacade.deletePost(projectId, userId, postId);
        return ApiResponse.ok();
    }

    // 게시글 프리뷰 조회
    @GetMapping("/preview")
    public ApiResponse<PostsPreviewResDto> getPostsPreview(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) PostType type,
            @RequestParam(defaultValue = "4") int limit
    ) {
        return ApiResponse.ok(
                postFacade.getPostsPreview(projectId, userDetails.getUserId(), type, limit)
        );
    }

}
