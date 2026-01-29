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
@Table(
        name = "process_mention",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_process_mention_process_user",
                        columnNames = {"process_id", "mentioned_user_id"}
                )
        }
)
public class ProcessMention extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="process_id", nullable=false)
    private Process process;

    // TODO : User엔티티 만들어지면 @ManyToOne User mentionedUser로 바꾸기
    @Column(name="mentioned_user_id", nullable=false)
    private Long mentionedUserId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ProcessMention(Process process, Long mentionedUserId) {
        this.process = process;
        this.mentionedUserId = mentionedUserId;
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

    public void restore() {
        this.deletedAt = null;
    }
}
