package com.nect.core.entity.team;


import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "custom_role_field_name", length = 50)
    private String customRoleFieldName;

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private ProjectTeamRole(Project project, RoleField roleField, String customRoleFieldName, Integer requiredCount) {
        if (roleField == null) {
            throw new IllegalArgumentException("roleField는 null일 수 없습니다.");
        }
        if (roleField == RoleField.CUSTOM && (customRoleFieldName == null || customRoleFieldName.isBlank())) {
            throw new IllegalArgumentException("CUSTOM이면 customRoleFieldName(직접입력)이 필수입니다.");
        }

        this.project = project;
        this.roleField = roleField;
        this.customRoleFieldName = (roleField == RoleField.CUSTOM) ? customRoleFieldName : null;
        this.requiredCount = requiredCount;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

}