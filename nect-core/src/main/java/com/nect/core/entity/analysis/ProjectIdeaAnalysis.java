package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "project_idea_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectIdeaAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;


    //TODO : 정규화 위반이긴하지만 추천명 3개를 담겠다고 별도의 엔티티를 만드는게 성능적으로 더 별로라 생각해서 별도 필드로 구현했습니다.
    @Column(name = "recommended_project_name_1", length = 100, nullable = false)
    private String recommendedProjectName1;

    @Column(name = "recommended_project_name_2", length = 100)
    private String recommendedProjectName2;

    @Column(name = "recommended_project_name_3", length = 100)
    private String recommendedProjectName3;

    @Column(name = "project_start_date")
    private LocalDate projectStartDate;

    @Column(name = "project_end_date")
    private LocalDate projectEndDate;

    @Column(name = "total_weeks")
    private Integer totalWeeks;

    // 보완점
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisImprovementPoint> improvementPoints = new ArrayList<>();
    // 팀 구성
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisTeamComposition> teamCompositions = new ArrayList<>();
    //로드맵
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisWeeklyRoadmap> weeklyRoadmaps = new ArrayList<>();


    @Builder
    public ProjectIdeaAnalysis(Long userId,
                               String recommendedProjectName1,
                               String recommendedProjectName2,
                               String recommendedProjectName3,
                               LocalDate projectStartDate,
                               LocalDate projectEndDate,
                               Integer totalWeeks) {
        this.userId = userId;
        this.recommendedProjectName1 = recommendedProjectName1;
        this.recommendedProjectName2 = recommendedProjectName2;
        this.recommendedProjectName3 = recommendedProjectName3;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
        this.totalWeeks = totalWeeks;
    }

    public List<String> getRecommendedProjectNames() {
        List<String> names = new ArrayList<>();
        names.add(recommendedProjectName1);
        if (recommendedProjectName2 != null) {
            names.add(recommendedProjectName2);
        }
        if (recommendedProjectName3 != null) {
            names.add(recommendedProjectName3);
        }
        return names;
    }

    public void addTeamComposition(AnalysisTeamComposition teamComposition) {
        this.teamCompositions.add(teamComposition);
        teamComposition.setAnalysis(this);
    }

    public void addImprovementPoint(AnalysisImprovementPoint improvementPoint) {
        this.improvementPoints.add(improvementPoint);
        improvementPoint.setAnalysis(this);
    }

    public void addWeeklyRoadmap(AnalysisWeeklyRoadmap weeklyRoadmap) {
        this.weeklyRoadmaps.add(weeklyRoadmap);
        weeklyRoadmap.setAnalysis(this);
    }

    public Set<RoleField> getRequiredRoleFields() {
        return teamCompositions.stream()
                .map(AnalysisTeamComposition::getRoleField)
                .collect(Collectors.toSet());
    }
    public Set<RoleField> getRoleFieldsForWeek(Integer weekNumber) {
        return weeklyRoadmaps.stream()
                .filter(roadmap -> roadmap.getWeekNumber().equals(weekNumber))
                .findFirst()
                .map(roadmap -> roadmap.getRoleTasks().stream()
                        .map(AnalysisRoleTask::getRoleField)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

}