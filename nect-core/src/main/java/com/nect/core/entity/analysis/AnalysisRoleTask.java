package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analysis_role_task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisRoleTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_roadmap_id", nullable = false)
    private AnalysisWeeklyRoadmap weeklyRoadmap;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false)
    private RoleField roleField;

    @Column(name = "tasks", columnDefinition = "TEXT", nullable = false)
    private String tasks;

    @Builder
    public AnalysisRoleTask(RoleField roleField, String tasks) {
        this.roleField = roleField;
        this.tasks = tasks;
    }

    public void setWeeklyRoadmap(AnalysisWeeklyRoadmap weeklyRoadmap) {
        this.weeklyRoadmap = weeklyRoadmap;
    }
}