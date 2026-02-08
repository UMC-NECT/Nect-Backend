package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.workspace.dto.req.PostLinkCreateReqDto;
import com.nect.api.domain.team.workspace.dto.res.PostAttachmentResDto;
import com.nect.api.domain.team.workspace.enums.PostErrorCode;
import com.nect.api.domain.team.workspace.exception.PostException;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.workspace.Post;
import com.nect.core.entity.team.workspace.PostSharedDocument;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
import com.nect.core.repository.team.workspace.PostRepository;
import com.nect.core.repository.team.workspace.PostSharedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class PostAttachmentService {
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;

    private final PostRepository postRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final PostSharedDocumentRepository postSharedDocumentRepository;

    private final ProjectHistoryPublisher historyPublisher;

    private void assertActiveMember(Long projectId, Long userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new PostException(PostErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId);
        }
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new PostException(PostErrorCode.PROJECT_MEMBER_FORBIDDEN, "projectId=" + projectId + ", userId=" + userId);
        }
    }

    private Post getActivePost(Long projectId, Long postId) {
        return postRepository.findByIdAndProjectIdAndDeletedAtIsNull(postId, projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND, "postId=" + postId));
    }

    private SharedDocument getActiveDocument(Long projectId, Long documentId) {
        return sharedDocumentRepository.findByIdAndProjectIdAndDeletedAtIsNull(documentId, projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.INVALID_REQUEST,
                        "document not found. projectId=" + projectId + ", documentId=" + documentId));
    }

    private void assertAuthor(Post post, Long userId) {
        Long authorId = post.getAuthor().getUserId();
        if (!Objects.equals(authorId, userId)) {
            throw new PostException(PostErrorCode.POST_AUTHOR_FORBIDDEN, "postId=" + post.getId() + ", userId=" + userId);
        }
    }

    // 파일 첨부 서비스
    @Transactional
    public PostAttachmentResDto attachFile(Long projectId, Long userId, Long postId, Long documentId) {
        assertActiveMember(projectId, userId);

        Post post = getActivePost(projectId, postId);
        assertAuthor(post, userId);

        SharedDocument doc = getActiveDocument(projectId, documentId);

        if (doc.getDocumentType() != DocumentType.FILE) {
            throw new PostException(PostErrorCode.INVALID_REQUEST,
                    "only FILE can be attached by attachFile. documentId=" + doc.getId()
            );
        }

        if (postSharedDocumentRepository.existsByPostIdAndDocumentIdAndDeletedAtIsNull(postId, documentId)) {
            throw new PostException(PostErrorCode.INVALID_REQUEST,
                    "already attached. postId=" + postId + ", documentId=" + documentId
            );
        }

        PostSharedDocument psd = PostSharedDocument.builder()
                .post(post)
                .document(doc)
                .attachedAt(LocalDateTime.now())
                .build();

        postSharedDocumentRepository.save(psd);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("postId", postId);
        meta.put("documentId", doc.getId());
        meta.put("type", "FILE");

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_ATTACHED,
                HistoryTargetType.POST,
                postId,
                meta
        );

        // downloadUrl은 Facade에서 presigned 만들어서 채워줄 수도 있음(아래 Facade에서 처리)
        return new PostAttachmentResDto(
                doc.getId(),
                doc.getDocumentType(),
                doc.getTitle(),
                null,
                doc.getFileName(),
                doc.getFileExt(),
                doc.getFileSize(),
                null
        );
    }

    // 링크 생성 및 첨부
    @Transactional
    public PostAttachmentResDto createAndAttachLink(Long projectId, Long userId, Long postId, PostLinkCreateReqDto req, User actor) {
        assertActiveMember(projectId, userId);

        if (req == null || req.linkUrl() == null || req.linkUrl().isBlank()) {
            throw new PostException(PostErrorCode.INVALID_REQUEST, "link_url is required");
        }
        if (req.title() == null || req.title().isBlank()) {
            throw new PostException(PostErrorCode.INVALID_REQUEST, "title is required");
        }

        Post post = getActivePost(projectId, postId);
        assertAuthor(post, userId);

        SharedDocument doc = SharedDocument.ofLink(
                actor,
                post.getProject(),
                req.title().trim(),
                req.linkUrl().trim()
        );
        SharedDocument saved = sharedDocumentRepository.save(doc);

        PostSharedDocument psd = PostSharedDocument.builder()
                .post(post)
                .document(saved)
                .attachedAt(LocalDateTime.now())
                .build();

        postSharedDocumentRepository.save(psd);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("postId", postId);
        meta.put("documentId", saved.getId());
        meta.put("type", "LINK");
        meta.put("title", saved.getTitle());
        meta.put("url", saved.getLinkUrl());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_ATTACHED,
                HistoryTargetType.POST,
                postId,
                meta
        );

        return new PostAttachmentResDto(
                saved.getId(),
                saved.getDocumentType(),
                saved.getTitle(),
                saved.getLinkUrl(),
                null,
                null,
                0L,
                null
        );
    }

    // 파일, 링크 첨부 해제
    @Transactional
    public void detach(Long projectId, Long userId, Long postId, Long documentId) {
        assertActiveMember(projectId, userId);

        Post post = getActivePost(projectId, postId);
        assertAuthor(post, userId);

        PostSharedDocument psd = postSharedDocumentRepository
                .findByPostIdAndDocumentIdAndDeletedAtIsNull(postId, documentId)
                .orElseThrow(() -> new PostException(PostErrorCode.INVALID_REQUEST,
                        "not attached. postId=" + postId + ", documentId=" + documentId));

        psd.softDelete();

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("postId", postId);
        meta.put("documentId", documentId);

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.DOCUMENT_DETACHED,
                HistoryTargetType.POST,
                postId,
                meta
        );
    }

}
