package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_idea_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProjectIdeaAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "project_type", length = 100)
    private String projectType;

    @Column(name = "tech_stack", columnDefinition = "TEXT")
    private String techStack;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "project_duration", length = 50)
    private String projectDuration;

    @Column(name = "user_experience_level", length = 50)
    private String userExperienceLevel;

    @Column(name = "user_interests", columnDefinition = "TEXT")
    private String userInterests;

    // AI 분석 결과
    @Column(name = "analysis_result", columnDefinition = "TEXT")
    private String analysisResult;

    @Column(name = "recommended_projects", columnDefinition = "TEXT")
    private String recommendedProjects;

    @Column(name = "learning_path", columnDefinition = "TEXT")
    private String learningPath;

    @Column(name = "team_composition", columnDefinition = "TEXT")
    private String teamComposition;

}