package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_weekly_roadmap")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisWeeklyRoadmap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ProjectIdeaAnalysis analysis;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "week_title", nullable = false)
    private String weekTitle;

    // 위크미션에서 필요할 거 같아서 필드 추가
    @Column(name = "week_start_date")
    private LocalDate weekStartDate;

    @Column(name = "week_end_date")
    private LocalDate weekEndDate;


    @OneToMany(mappedBy = "weeklyRoadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisRoleTask> roleTasks = new ArrayList<>();

    @Builder
    public AnalysisWeeklyRoadmap(Integer weekNumber, String weekTitle,
                                 LocalDate weekStartDate, LocalDate weekEndDate) {
        this.weekNumber = weekNumber;
        this.weekTitle = weekTitle;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
    }

    public void setAnalysis(ProjectIdeaAnalysis analysis) {
        this.analysis = analysis;
    }

    public void addRoleTask(AnalysisRoleTask roleTask) {
        this.roleTasks.add(roleTask);
        roleTask.setWeeklyRoadmap(this);
    }
}

