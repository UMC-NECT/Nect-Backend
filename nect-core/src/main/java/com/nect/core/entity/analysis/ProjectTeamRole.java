package com.nect.core.entity.analysis;


import com.nect.core.entity.BaseEntity;
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

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false)
    private RoleField roleField;

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    @Builder
    public ProjectTeamRole(Long projectId, RoleField roleField, Integer requiredCount) {
        this.projectId = projectId;
        this.roleField = roleField;
        this.requiredCount = requiredCount;
    }
}