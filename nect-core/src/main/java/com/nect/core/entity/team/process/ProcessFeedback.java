package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.process.enums.ProcessFeedbackStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "process_feedback")
public class ProcessFeedback extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="process_id", nullable=false)
    private Process process;

    // TODO
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by", nullable = false)
//    private User createdBy;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessFeedbackStatus status;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // TODO : User createdBy 추가하기
    @Builder
    private ProcessFeedback(Process process, String content) {
        this.process = process;
//        this.createdBy = createdBy;
        this.content = content;
        this.status = ProcessFeedbackStatus.OPEN;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    void setProcess(Process process) {
        this.process = process;
    }

    public void resolve() {
        this.status = ProcessFeedbackStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void reopen() {
        this.status = ProcessFeedbackStatus.OPEN;
        this.resolvedAt = null;
    }
}
