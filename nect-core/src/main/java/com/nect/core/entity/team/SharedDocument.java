package com.nect.core.entity.team;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.user.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_ext", length = 10)
    private FileExt fileExt;

    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private SharedDocument(
            User createdBy,
            Project project,
            DocumentType documentType,
            String linkUrl,
            boolean isPinned,
            String title,
            String description,
            String fileName,
            FileExt fileExt,
            String fileUrl,
            Long fileSize
    ) {
        this.createdBy = createdBy;
        this.project = project;
        this.documentType = documentType;
        this.linkUrl = linkUrl;
        this.isPinned = isPinned;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.fileExt = fileExt;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
    }

    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }

    public static SharedDocument ofFile(User user, Project project, String title, String fileName, FileExt ext, String fileKey, Long size) {
        return SharedDocument.builder()
                .createdBy(user)
                .project(project)
                .documentType(DocumentType.FILE)
                .isPinned(false)
                .title(title)
                .fileName(fileName)
                .fileExt(ext)
                .fileUrl(fileKey)
                .fileSize(size)
                .linkUrl(null)
                .description(null)
                .build();
    }

    public static SharedDocument ofLink(User user, Project project, String title, String url) {
        return SharedDocument.builder()
                .createdBy(user)
                .project(project)
                .documentType(DocumentType.LINK)
                .isPinned(false)
                .title(title)
                .fileName(null)
                .linkUrl(url)
                .fileExt(null)
                .fileUrl(null)
                .fileSize(0L)
                .description(null)
                .build();
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

}
