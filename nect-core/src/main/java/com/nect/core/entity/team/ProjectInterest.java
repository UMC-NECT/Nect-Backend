package com.nect.core.entity.team;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.InterestField;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "team_interest_field",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "interest_field"})
)
public class ProjectInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_field", nullable = false)
    private InterestField interestField;

    @Column(name = "is_selected", nullable = false)
    private Boolean selected;

    @Builder
    public ProjectInterest(Project project, InterestField interestField, boolean selected) {
        this.project = project;
        this.interestField = interestField;
        this.selected = selected;
    }

    public void changeSelected(boolean selected) {
        this.selected = selected;
    }
}
