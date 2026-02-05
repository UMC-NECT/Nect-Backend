package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_weekly_plan")
public class ProjectWeeklyPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "week_title", nullable = false)
    private String weekTitle;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Builder
    public ProjectWeeklyPlan(Long projectId, Integer weekNumber, String weekTitle,
                             LocalDate weekStartDate, LocalDate weekEndDate) {
        this.projectId = projectId;
        this.weekNumber = weekNumber;
        this.weekTitle = weekTitle;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
    }
}