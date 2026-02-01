package com.nect.core.entity.team.workspace;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.workspace.enums.WorkStatus;
import com.nect.core.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "project_user_work_daily",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_work_daily_project_user_date",
                        columnNames = {"project_id", "user_id", "work_date"}
                )
        }
)
public class ProjectUserWorkDaily extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 오늘 날짜(누적 기준)
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // WORKING/PAUSED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkStatus status;

    // 오늘 누적(초) - stop 할 때만 쌓임
    @Column(name = "accumulated_seconds", nullable = false)
    private Long accumulatedSeconds;

    // 작업중일 때만 값 존재
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Builder
    private ProjectUserWorkDaily(Project project, User user, LocalDate workDate) {
        this.project = project;
        this.user = user;
        this.workDate = workDate;
        this.status = WorkStatus.PAUSED;
        this.accumulatedSeconds = 0L;
        this.startedAt = null;
    }

    public boolean isWorking() {
        return this.status == WorkStatus.WORKING;
    }

    public void start(LocalDateTime now) {
        if (this.isWorking()) return;
        this.status = WorkStatus.WORKING;
        this.startedAt = now;
    }

    public void stop(LocalDateTime now) {
        if (!this.isWorking()) return;
        if (this.startedAt != null) {
            long delta = java.time.Duration.between(this.startedAt, now).getSeconds();
            if (delta > 0) this.accumulatedSeconds += delta;
        }
        this.status = WorkStatus.PAUSED;
        this.startedAt = null;
    }
}
