package com.nect.core.entity.analysis;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analysis_team_composition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisTeamComposition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ProjectIdeaAnalysis analysis;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false)
    private RoleField roleField;

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    @Builder
    public AnalysisTeamComposition(RoleField roleField, Integer requiredCount) {
        this.roleField = roleField;
        this.requiredCount = requiredCount;
    }

    public void setAnalysis(ProjectIdeaAnalysis analysis) {
        this.analysis = analysis;
    }
}