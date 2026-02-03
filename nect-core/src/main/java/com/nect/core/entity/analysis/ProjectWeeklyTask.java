package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_weekly_task")
public class ProjectWeeklyTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weekly_plan_id", nullable = false)
    private Long weeklyPlanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false)
    private RoleField roleField;

    @Column(name = "tasks", columnDefinition = "TEXT", nullable = false)
    private String tasks;

    @Builder
    public ProjectWeeklyTask(Long weeklyPlanId, RoleField roleField, String tasks) {
        this.weeklyPlanId = weeklyPlanId;
        this.roleField = roleField;
        this.tasks = tasks;
    }
}