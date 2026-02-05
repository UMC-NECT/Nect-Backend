package com.nect.core.entity.team.process;


import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_team_role")
public class ProjectTeamRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false)
    private RoleField roleField;

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    @Builder
    private ProjectTeamRole(Project project, RoleField roleField, Integer requiredCount) {
        this.project = project;
        this.roleField = roleField;
        this.requiredCount = requiredCount;
    }


}