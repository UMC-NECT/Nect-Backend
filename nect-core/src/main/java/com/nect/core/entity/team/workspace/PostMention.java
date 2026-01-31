package com.nect.core.entity.team.workspace;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_mention",
        indexes = {
                @Index(name = "idx_post_mention_post_id", columnList = "post_id"),
                @Index(name = "idx_post_mention_user_id", columnList = "mentioned_user_id")
        }
)
public class PostMention extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글의 멘션인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "mentioned_user_id", nullable = false)
    private Long mentionedUserId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private PostMention(Post post, Long mentionedUserId) {
        this.post = post;
        this.mentionedUserId = mentionedUserId;
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
