package com.nect.core.entity.team.workspace;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.workspace.enums.PostType;
import com.nect.core.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Post(User author,
                 Project project,
                 PostType postType,
                 String title,
                 String content,
                 Boolean isPinned) {
        this.author = author;
        this.project = project;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.isPinned = (isPinned != null) ? isPinned : false;
    }

    public void update(PostType postType,
                       String title,
                       String content,
                       Boolean isPinned) {
        if (postType != null) this.postType = postType;
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (isPinned != null) this.isPinned = isPinned;
    }

    // 좋아요 증가
    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null ? 0L : this.likeCount) + 1L;
    }

    // 좋아요 감소
    public void decreaseLikeCount() {
        long current = (this.likeCount == null ? 0L : this.likeCount);
        this.likeCount = Math.max(0L, current - 1L);
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
