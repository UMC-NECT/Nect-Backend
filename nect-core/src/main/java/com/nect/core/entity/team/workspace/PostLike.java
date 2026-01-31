package com.nect.core.entity.team.workspace;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_comment_like_comment_member",
                        columnNames = {"post_id", "user_id"}
                )
        }
)
public class PostLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글에 좋아요인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

//    // 누가 좋아요 했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    void setPost(Post post) { this.post = post; }
    void setUser(User user) { this.user = user; }

}
