package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "process_task_item")
public class ProcessTaskItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="process_id", nullable=false)
    private Process process;

    @Column(name = "content",nullable = false)
    private String content;

    @Column(name = "is_done")
    private boolean isDone;

    @Column(name = "done_at")
    private LocalDate doneAt;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ProcessTaskItem(Process process, String content, boolean isDone, Integer sortOrder) {
        this.process = process;
        this.content = content;
        this.isDone = isDone;
        this.sortOrder = (sortOrder != null) ? sortOrder : 0;
        this.doneAt = isDone ? LocalDate.now() : null;
    }

    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    public void updateDone(boolean done) {
        this.isDone = done;
        this.doneAt = done ? LocalDate.now() : null;
    }

    public void updateSortOrder(Integer sortOrder) {
        if (sortOrder != null) this.sortOrder = sortOrder;
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
