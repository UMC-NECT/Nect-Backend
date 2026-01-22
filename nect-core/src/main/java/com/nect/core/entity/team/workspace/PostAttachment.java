package com.nect.core.entity.team.workspace;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.SharedDocument;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post_attachment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_attachment_post_document", columnNames = {"post_id", "document_id"})
        }
)
public class PostAttachment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private SharedDocument document;

    void setPost(Post post) {
        this.post = post;
    }

    void setDocument(SharedDocument document) {
        this.document = document;
    }
}
