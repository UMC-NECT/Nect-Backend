package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.file.dto.res.FileDownloadUrlResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.api.domain.team.workspace.dto.req.PostCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.PostUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.*;
import com.nect.api.domain.team.workspace.enums.PostSort;
import com.nect.api.domain.team.workspace.service.PostService;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.workspace.enums.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostFacade {

    private final PostService postService;
    private final FileService fileService;

    // 게시글 생성
    public PostCreateResDto createPost(Long projectId, Long userId, PostCreateReqDto req) {
        return postService.createPost(projectId, userId, req);
    }

    // 게시글 조회
    public PostGetResDto getPost(Long projectId, Long userId, Long postId) {
        PostGetResDto base = postService.getPost(projectId, userId, postId);

        List<PostAttachmentResDto> filled = (base.attachments() == null)
                ? List.of()
                : base.attachments().stream()
                .map(a -> {
                            if (a.documentType() != DocumentType.FILE) {
                                return a;
                            }

                    FileDownloadUrlResDto urlDto =
                            fileService.getDownloadUrl(projectId, userId, a.documentId());

                    return new PostAttachmentResDto(
                            a.documentId(),
                            a.documentType(),
                            a.title(),
                            a.linkUrl(),
                            a.fileName(),
                            a.fileExt(),
                            a.fileSize(),
                            urlDto.downloadUrl()
                    );
                })
                .toList();

        return new PostGetResDto(
                base.postId(),
                base.postType(),
                base.title(),
                base.content(),
                base.likeCount(),
                base.createdAt(),
                base.author(),
                filled
        );
    }

    // 게시글 목록 조회
    public PostListResDto getPostList(Long projectId, Long userId, PostType type,  int page, int size) {
        return postService.getPostList(projectId, userId, type, page, size);
    }

    // 게시글 수정
    public PostUpdateResDto updatePost(Long projectId, Long userId, Long postId, PostUpdateReqDto req) {
        return postService.updatePost(projectId, userId, postId, req);
    }

    // 게시글 좋아요 토글
    public PostLikeToggleResDto togglePostLike(Long projectId, Long userId, Long postId) {
        return postService.togglePostLike(projectId, userId, postId);
    }

    // 게시글 삭제
    public void deletePost(Long projectId, Long userId, Long postId) {
        postService.deletePost(projectId, userId, postId);
    }

    // 게시글 목록 프리뷰
    public PostsPreviewResDto getPostsPreview(Long projectId, Long userId, PostType type, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        PostListResDto list = postService.getPostList(
                projectId,
                userId,
                type,
                0,
                safeLimit
        );

        List<PostsPreviewResDto.Item> items = list.posts().stream()
                .map(p -> new PostsPreviewResDto.Item(
                        p.postId(),
                        p.postType(),
                        p.title(),
                        p.createdAt()
                ))
                .toList();

        return new PostsPreviewResDto(items);
    }
}