package com.nect.core.entity.team;

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
@Table(name = "shared_document")
public class SharedDocument extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by", nullable = false)
//    private User createdBy;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_name", length = 200, nullable = false)
    private String fileName;

    @Column(name = "file_ext", length = 10, nullable = false)
    private String fileExt; // jpg, png, pdf, zip

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // TODO : 매개변수 User createdBy 추가
    @Builder
    private SharedDocument(
                           boolean isPinned,
                           String title,
                           String description,
                           String fileName,
                           String fileExt,
                           String fileUrl) {
//        this.createdBy = createdBy;
        this.isPinned = isPinned;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.fileExt = fileExt;
        this.fileUrl = fileUrl;
    }

    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
