package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analysis_improvement_point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisImprovementPoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ProjectIdeaAnalysis analysis;

    @Column(name = "point_order", nullable = false)
    private Integer pointOrder;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder
    public AnalysisImprovementPoint(Integer pointOrder, String title, String description) {
        this.pointOrder = pointOrder;
        this.title = title;
        this.description = description;
    }

    public void setAnalysis(ProjectIdeaAnalysis analysis) {
        this.analysis = analysis;
    }
}
