package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_improvement_point")
public class ProjectImprovementPoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "point_order", nullable = false)
    private Integer pointOrder;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder
    public ProjectImprovementPoint(Long projectId, Integer pointOrder,
                                   String title, String description) {
        this.projectId = projectId;
        this.pointOrder = pointOrder;
        this.title = title;
        this.description = description;
    }
}