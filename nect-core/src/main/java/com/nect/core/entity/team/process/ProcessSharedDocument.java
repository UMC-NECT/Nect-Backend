package com.nect.core.entity.team.process;

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
        name = "process_shared_document",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_process_document", columnNames = {"process_id", "document_id"})
        }
)
public class ProcessSharedDocument extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private SharedDocument document;

    @Column(name = "attached_at", nullable = false)
    private LocalDateTime attachedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ProcessSharedDocument(Process process, SharedDocument document, LocalDateTime attachedAt) {
        this.process = process;
        this.document = document;
        this.attachedAt = (attachedAt == null ? LocalDateTime.now() : attachedAt);
    }

    void setProcess(Process process) {
        this.process = process;
    }

    void setDocument(SharedDocument document) {
        this.document = document;
    }


    public void softDelete() {
        if (this.deletedAt != null) return;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
