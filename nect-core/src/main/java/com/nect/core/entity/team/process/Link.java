package com.nect.core.entity.team.process;

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
@Table(name = "link")
public class Link extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="process_id", nullable=false)
    private Process process;

    @Column(name="title", length=50, nullable=false)
    private String title;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Link(Process process, String title, String url) {
        this.process = process;
        this.title = title;
        this.url = url;
    }

    void setProcess(Process process) {
        this.process = process;
    }


    public void softDelete() {
        if (this.deletedAt != null) return;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
