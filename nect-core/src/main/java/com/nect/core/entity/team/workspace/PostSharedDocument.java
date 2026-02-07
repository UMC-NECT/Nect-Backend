package com.nect.core.entity.team.workspace;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.SharedDocument;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post_shared_document",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"post_id", "document_id"})
        }
)
public class PostSharedDocument extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private SharedDocument document;

    @Column(name = "attached_at", nullable = false)
    private LocalDateTime attachedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private PostSharedDocument(Post post, SharedDocument document, LocalDateTime attachedAt) {
        this.post = post;
        this.document = document;
        this.attachedAt = attachedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
