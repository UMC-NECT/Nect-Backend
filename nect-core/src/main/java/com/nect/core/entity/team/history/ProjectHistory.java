package com.nect.core.entity.team.history;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.entity.team.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_history",
        indexes = {
                @Index(name = "idx_project_history_project_id_id", columnList = "project_id, id")
        })
public class ProjectHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private HistoryAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private HistoryTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    // TEXT or jsonb
    @Column(name = "meta_json", columnDefinition = "jsonb")
    private String metaJson;

    @Builder
    private ProjectHistory(Project project,
                           Long actorUserId,
                           HistoryAction action,
                           HistoryTargetType targetType,
                           Long targetId,
                           String metaJson) {
        this.project = project;
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metaJson = metaJson;
    }
}
