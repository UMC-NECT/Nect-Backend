package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.notifications.command.NotificationCommand;
import com.nect.api.domain.notifications.facade.NotificationFacade;
import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.process.dto.res.AttachmentDto;
import com.nect.api.domain.team.workspace.dto.req.PostCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.PostUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.*;
import com.nect.api.domain.team.workspace.enums.PostErrorCode;
import com.nect.api.domain.team.workspace.enums.PostSort;
import com.nect.api.domain.team.workspace.exception.PostException;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.workspace.Post;
import com.nect.core.entity.team.workspace.PostLike;
import com.nect.core.entity.team.workspace.PostMention;
import com.nect.core.entity.team.workspace.PostSharedDocument;
import com.nect.core.entity.team.workspace.enums.PostType;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.workspace.PostLikeRepository;
import com.nect.core.repository.team.workspace.PostMentionRepository;
import com.nect.core.repository.team.workspace.PostRepository;
import com.nect.core.repository.team.workspace.PostSharedDocumentRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMentionRepository postMentionRepository;
    private final PostSharedDocumentRepository postSharedDocumentRepository;

    private final ProjectHistoryPublisher historyPublisher;
    private final NotificationFacade notificationFacade;

    private List<User> validateAndLoadMentionReceivers(Long projectId, Long actorId, List<Long> mentionIds) {
        if (mentionIds == null) return List.of();

        List<Long> ids = mentionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> !id.equals(actorId))
                .toList();

        if (ids.isEmpty()) return List.of();

        long cnt = projectUserRepository.countByProject_IdAndUserIdIn(projectId, ids);
        if (cnt != ids.size()) {
            throw new PostException(
                    PostErrorCode.INVALID_REQUEST,
                    "mention_user_ids contains non-member user. projectId=" + projectId
            );
        }

        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new PostException(PostErrorCode.USER_NOT_FOUND, "mentioned user not found");
        }

        return users;
    }

    private void notifyBoardMention(Project project, User actor, Long targetBoardId, List<User> receivers, String content) {
        if (receivers == null || receivers.isEmpty()) return;

        NotificationCommand command = new NotificationCommand(
                NotificationType.WORKSPACE_MENTIONED,
                NotificationClassification.BOARD,
                NotificationScope.WORKSPACE_ONLY,
                targetBoardId,
                new Object[]{ actor.getName() },
                new Object[]{ content },
                project
        );

        notificationFacade.notify(receivers, command);
    }

    private void validateMaxLength(String value, int max, String fieldName) {
        if (value == null) return;
        // 공백 포함 길이(문자 수) 기준
        int len = value.codePointCount(0, value.length());
        if (len > max) {
            throw new PostException(PostErrorCode.INVALID_REQUEST,
                    fieldName + " is too long. max=" + max + ", actual=" + len);
        }
    }

    private PostType resolvePostType(Boolean isNotice) {
        return Boolean.TRUE.equals(isNotice) ? PostType.NOTICE : PostType.FREE;
    }


    // 게시글 생성 서비스
    @Transactional
    public PostCreateResDto createPost(Long projectId, Long userId, PostCreateReqDto req) {

        // 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // ACTIVE 멤버인지
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new PostException(PostErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // 작성자
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new PostException(PostErrorCode.USER_NOT_FOUND, "userId=" + userId));

        if (req.title() == null || req.title().isBlank()) {
            throw new PostException(PostErrorCode.INVALID_REQUEST, "title is blank");
        }
        if (req.content() == null || req.content().isBlank()) {
            throw new PostException(PostErrorCode.INVALID_REQUEST, "content is blank");
        }

        // 길이 제한(공백 포함)
        validateMaxLength(req.title(), 200, "title");
        validateMaxLength(req.content(), 1000, "content");

        PostType postType = resolvePostType(req.isNotice());


        // 게시글 생성
        Post post = Post.builder()
                .author(author)
                .project(project)
                .postType(postType)
                .title(req.title())
                .content(req.content())
                .build();

        Post saved = postRepository.save(post);

        // 멘션 동기화
        List<Long> mentionIds = Optional.ofNullable(req.mentionUserIds())
                .orElse(List.of())
                .stream().filter(Objects::nonNull).distinct().toList();

        syncMentions(saved, mentionIds, projectId);

        // 멘션된 사람들에게 알림
        List<User> mentionReceivers = validateAndLoadMentionReceivers(projectId, userId, mentionIds);
        notifyBoardMention(project, author, saved.getId(), mentionReceivers, post.getTitle());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.POST_CREATED,
                HistoryTargetType.POST,
                saved.getId(),
                Map.of(
                        "postType", saved.getPostType().name(),
                        "title", saved.getTitle(),
                        "mentionUserIds", mentionIds
                )
        );

        return new PostCreateResDto(saved.getId());
    }

    // 게시글 상세 조회 서비스
    @Transactional(readOnly = true)
    public PostGetResDto getPost(Long projectId, Long userId, Long postId) {

        // 프로젝트 존재 확인
        projectRepository.findById(projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // 멤버 권한 확인
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new PostException(PostErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // 게시글 조회
        Post post = postRepository.findByIdAndProjectIdAndDeletedAtIsNull(postId, projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.INVALID_REQUEST,
                        "post not found or not in project. projectId=" + projectId + ", postId=" + postId));

        // attachments 조회
        List<PostSharedDocument> psds = postSharedDocumentRepository.findAllActiveByPostIdWithDocument(postId);

        List<PostAttachmentResDto> attachments = psds.stream()
                .map(psd -> {
                    SharedDocument d = psd.getDocument();


                    return new PostAttachmentResDto(
                            d.getId(),
                            d.getDocumentType(),
                            d.getTitle(),
                            d.getLinkUrl(),
                            d.getFileName(),
                            d.getFileExt(),
                            d.getFileSize(),
                            null
                    );
                })
                .toList();

        return new PostGetResDto(
                post.getId(),
                post.getPostType(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                post.getCreatedAt(),
                new PostGetResDto.AuthorDto(
                        post.getAuthor().getUserId(),
                        post.getAuthor().getName(),
                        post.getAuthor().getNickname()
                ),
                attachments
        );
    }



    private String preview(String content, int maxLen) {
        if (content == null) return "";
        String c = content.strip();
        if (c.length() <= maxLen) return c;
        return c.substring(0, maxLen) + "...";
    }

    // 게시글 목록 조회 서비스
    @Transactional(readOnly = true)
    public PostListResDto getPostList(Long projectId, Long userId, PostType type, int page, int size) {
        // 프로젝트 존재 확인
        projectRepository.findById(projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // 멤버 권한 확인
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new PostException(PostErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // page 검증
        if (page < 0) {
            throw new PostException(PostErrorCode.INVALID_REQUEST, "invalid page");
        }

        int fixedSize = Math.min(Math.max(size, 1), 50);

        Sort baseSort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

        // 공지만
        if (type == PostType.NOTICE) {
            List<Post> notices = postRepository.findAllNotices(projectId, baseSort);

            List<PostListResDto.PostSummaryDto> mapped = notices.stream()
                    .map(p -> new PostListResDto.PostSummaryDto(
                            p.getId(),
                            p.getPostType(),
                            p.getTitle(),
                            preview(p.getContent(), 100),
                            p.getLikeCount(),
                            p.getCreatedAt()
                    ))
                    .toList();

            PostListResDto.PageInfo pageInfo = new PostListResDto.PageInfo(
                    0,
                    fixedSize,
                    mapped.size(),
                    1,
                    false
            );
            return new PostListResDto(mapped, pageInfo);
        }


        // FREE만 페이지네이션
        Pageable pageable = PageRequest.of(page, fixedSize, baseSort);
        Page<Post> freePage = postRepository.findFreePosts(projectId, pageable);

        List<PostListResDto.PostSummaryDto> result = new java.util.ArrayList<>();

        // page==0 일 때만 공지 전부 상단에 붙이기
        if (type == null && page == 0) {
            List<Post> notices = postRepository.findAllNotices(projectId, baseSort);
            result.addAll(notices.stream()
                    .map(p -> new PostListResDto.PostSummaryDto(
                            p.getId(),
                            p.getPostType(),
                            p.getTitle(),
                            preview(p.getContent(), 100),
                            p.getLikeCount(),
                            p.getCreatedAt()
                    ))
                    .toList());
        }

        // FREE 페이징 결과 붙이기
        result.addAll(freePage.getContent().stream()
                .map(p -> new PostListResDto.PostSummaryDto(
                        p.getId(),
                        p.getPostType(),
                        p.getTitle(),
                        preview(p.getContent(), 100),
                        p.getLikeCount(),
                        p.getCreatedAt()
                ))
                .toList());

        // pageInfo는 FREE 기준으로만 계산 (공지는 제외)
        PostListResDto.PageInfo pageInfo = new PostListResDto.PageInfo(
                freePage.getNumber(),
                freePage.getSize(),
                freePage.getTotalElements(),
                freePage.getTotalPages(),
                freePage.hasNext()
        );

        return new PostListResDto(result, pageInfo);
    }

    // 게시글 수정 서비스
    @Transactional
    public PostUpdateResDto updatePost(Long projectId, Long userId, Long postId, PostUpdateReqDto req) {
        if (req == null) {
            throw new PostException(PostErrorCode.INVALID_REQUEST, "request body is null");
        }

        // 프로젝트 검증
        projectRepository.findById(projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // 멤버 권한 확인
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!isMember) {
            throw new PostException(PostErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // 게시글 조회
        Post post = postRepository.findByIdAndProjectIdAndDeletedAtIsNull(postId, projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND, "postId=" + postId));

        // 작성자만 수정 가능
        Long authorId = post.getAuthor().getUserId();
        if(!authorId.equals(userId)) {
            throw new PostException(PostErrorCode.POST_AUTHOR_FORBIDDEN,
                    "postId=" + postId + ", userId=" + userId);
        }

        // 값 검증(빈문자 방지)
        if (req.title() != null && req.title().isBlank())
            throw new PostException(PostErrorCode.INVALID_REQUEST, "title is blank");
        if (req.content() != null && req.content().isBlank())
            throw new PostException(PostErrorCode.INVALID_REQUEST, "content is blank");

        // 길이 제한(공백 포함)
        validateMaxLength(req.title(), 200, "title");
        validateMaxLength(req.content(), 1000, "content");

        // 공지 토글: req.isNotice()가 들어온 경우에만 바꿈
        PostType newType = (req.isNotice() == null) ? null : resolvePostType(req.isNotice());

        // before 스냅샷
        final PostType beforeType = post.getPostType();
        final String beforeTitle = post.getTitle();
        final String beforeContent = post.getContent();

        final List<Long> beforeMentionIds = postMentionRepository.findAllByPostId(post.getId()).stream()
                .filter(m -> !m.isDeleted())
                .map(PostMention::getMentionedUserId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();


        post.update(newType, req.title(), req.content());

        // 멘션 교체 (null이면 변경 안 함 / []이면 전부 제거)
        List<Long> mentionIdsForRes = syncMentions(post, req.mentionUserIds(), projectId);

        // 멘션 알림 : 요청이 들어온 경우에만, 그리고 '새로 추가된 멘션'에게만 전송
        if (req.mentionUserIds() != null) {
            List<Long> afterIds = (mentionIdsForRes == null) ? List.of() : mentionIdsForRes.stream()
                    .filter(Objects::nonNull).distinct().toList();

            Set<Long> beforeSet = new HashSet<>(beforeMentionIds);
            List<Long> addedMentionIds = afterIds.stream()
                    .filter(id -> !beforeSet.contains(id))
                    .toList();

            if (!addedMentionIds.isEmpty()) {
                List<User> receivers = validateAndLoadMentionReceivers(projectId, userId, addedMentionIds);
                notifyBoardMention(post.getProject(), post.getAuthor(), post.getId(), receivers, post.getTitle());
            }
        }

        // after 스냅샷
        final PostType afterType = post.getPostType();
        final String afterTitle = post.getTitle();
        final String afterContent = post.getContent();

        final List<Long> afterMentionIds = (mentionIdsForRes == null)
                ? null
                : mentionIdsForRes.stream().filter(Objects::nonNull).distinct().sorted().toList();

        Map<String, Object> changed = new LinkedHashMap<>();

        if (beforeType != afterType) {
            changed.put("postType", Map.of("before", beforeType, "after", afterType));
        }
        if (!Objects.equals(beforeTitle, afterTitle)) {
            changed.put("title", Map.of("before", beforeTitle, "after", afterTitle));
        }
        if (!Objects.equals(beforeContent, afterContent)) {
            changed.put("content", Map.of("before", beforeContent, "after", afterContent));
        }

        if (afterMentionIds != null && !Objects.equals(beforeMentionIds, afterMentionIds)) {
            changed.put("mentions", Map.of("before", beforeMentionIds, "after", afterMentionIds));
        }

        if (changed.isEmpty()) {
            throw new PostException(PostErrorCode.INVALID_REQUEST, "no changes");
        }

        // 실제 변경이 있을 때만 publish
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("changed", changed);
        meta.put("before", Map.of(
                "postType", beforeType,
                "title", beforeTitle,
                "content", beforeContent
        ));
        meta.put("after", Map.of(
                "postType", afterType,
                "title", afterTitle,
                "content", afterContent,
                "mentionUserIds", (afterMentionIds != null ? afterMentionIds : beforeMentionIds)
        ));

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.POST_UPDATED,
                HistoryTargetType.POST,
                post.getId(),
                meta
        );

        return new PostUpdateResDto(post.getId(), post.getUpdatedAt());
    }

    // 게시글 좋아요 토글 서비스
    @Transactional
    public PostLikeToggleResDto togglePostLike(Long projectId, Long userId, Long postId) {

        // 프로젝트 검증
        projectRepository.findById(projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // 멤버 권한 확인
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new PostException(PostErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PostException(PostErrorCode.USER_NOT_FOUND, "userId=" + userId));


        Post post = postRepository.findByIdAndProjectIdAndDeletedAtIsNull(postId, projectId)
                .orElseThrow(() -> new PostException(
                        PostErrorCode.INVALID_REQUEST,
                        "post not found or not in project. projectId=" + projectId + ", postId=" + postId
                ));

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserUserId(postId, userId);

        if(alreadyLiked) {
            postLikeRepository.deleteByPostIdAndUserUserId(postId, userId);
            post.decreaseLikeCount();
            return new PostLikeToggleResDto(post.getId(), false, post.getLikeCount());
        }

        postLikeRepository.save(PostLike.of(post, user));
        post.increaseLikeCount();
        return new PostLikeToggleResDto(post.getId(), true, post.getLikeCount());


    }


    private List<Long> syncMentions(Post post, List<Long> requestedUserIds, Long projectId) {
        // null이면 언급 변경 안 함
        if (requestedUserIds == null) return null;

        // 빈 리스트면 전부 제거 의미
        List<Long> req = requestedUserIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 언급 대상이 전부 프로젝트 멤버인지 검증
        if (!req.isEmpty()) {
            long cnt = projectUserRepository.countByProject_IdAndUserIdIn(projectId, req);
            if (cnt != req.size()) {
                throw new PostException(
                        PostErrorCode.INVALID_REQUEST,
                        "mentioned user is not a project member. projectId=" + projectId
                );
            }
        }

        // 삭제 포함 전체 조회
        List<PostMention> all = postMentionRepository.findAllByPostId(post.getId());

        Map<Long, PostMention> byUserId = new HashMap<>();
        for (PostMention m : all) {
            byUserId.put(m.getMentionedUserId(), m);
        }

        // 요청에 없는 기존 mention -> soft delete
        for (PostMention m : all) {
            if (!req.contains(m.getMentionedUserId())) {
                m.softDelete();
            }
        }

        // 요청에 있는 mention -> restore or create
        for (Long uid : req) {
            PostMention existing = byUserId.get(uid);
            if (existing != null) {
                if (existing.isDeleted()) existing.restore();
            } else {
                PostMention created = PostMention.builder()
                        .post(post)
                        .mentionedUserId(uid)
                        .build();
                postMentionRepository.save(created);
            }
        }

        return req;
    }

    // 게시글 삭제 서비스
    @Transactional
    public void deletePost(Long projectId, Long userId, Long postId) {

        // 프로젝트 검증
        projectRepository.findById(projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // 멤버 권한 확인
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new PostException(PostErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // 게시글 조회
        Post post = postRepository.findByIdAndProjectIdAndDeletedAtIsNull(postId, projectId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND,
                        "projectId=" + projectId + ", postId=" + postId));

        // 작성자만 삭제 가능
        Long authorId = post.getAuthor().getUserId();
        if (!authorId.equals(userId)) {
            throw new PostException(PostErrorCode.POST_AUTHOR_FORBIDDEN,
                    "postId=" + postId + ", userId=" + userId);
        }

        // before 스냅샷
        final PostType beforeType = post.getPostType();
        final String beforeTitle = post.getTitle();

        // soft delete
        post.softDelete();

        // 멘션도 soft delete 처리
         postMentionRepository.findAllByPostId(post.getId()).forEach(PostMention::softDelete);

        // HISTORY 발행
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("postType", beforeType);
        meta.put("title", beforeTitle);
        meta.put("deletedAt", post.getDeletedAt());

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.POST_DELETED,
                HistoryTargetType.POST,
                post.getId(),
                meta
        );
    }
}
