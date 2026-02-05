package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.process.enums.ProcessStatus;
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
        name = "process_lane_order",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_process_lane_order",
                columnNames = {"project_id", "process_id", "lane_key", "status"}
        )
)
public class ProcessLaneOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    // TEAM은 null 금지
    @Column(name = "lane_key", nullable = false, length = 120)
    private String laneKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProcessStatus status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private ProcessLaneOrder(Long projectId, Process process, String laneKey, ProcessStatus status, Integer sortOrder) {
        this.projectId = projectId;
        this.process = process;
        this.laneKey = laneKey;
        this.status = status;
        this.sortOrder = (sortOrder == null) ? 0 : sortOrder;
    }

    public void updateSortOrder(int order) {
        this.sortOrder = order;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

