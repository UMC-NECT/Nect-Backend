package com.nect.core.entity.user;

import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "user_team_roles",
        indexes = {
                @Index(name = "idx_user_team_roles_project_id", columnList = "project_id"),
                @Index(name = "idx_user_team_roles_project_role", columnList = "project_id, role_field")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTeamRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 마이페이지 설정은 "프로젝트별"이어야 함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false, length = 50)
    private RoleField roleField;

    // CUSTOM일 때만 사용
    @Column(name = "custom_role_field_name", length = 50)
    private String customRoleFieldName;

    // UI의 “1명”
    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private UserTeamRole(Project project, RoleField roleField, String customRoleFieldName, Integer requiredCount) {
        this.project = project;
        this.roleField = roleField;
        this.customRoleFieldName = customRoleFieldName;
        this.requiredCount = requiredCount;
    }

    public static UserTeamRole of(Project project, RoleField roleField, String customRoleFieldName, Integer requiredCount) {
        return UserTeamRole.builder()
                .project(project)
                .roleField(roleField)
                .customRoleFieldName(customRoleFieldName)
                .requiredCount(requiredCount)
                .build();
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public String getLabel() {
        if (roleField == RoleField.CUSTOM) return customRoleFieldName;
        return roleField.getLabelEn();
    }

    public void updateCustomRoleName(String customRoleFieldName) {
        this.customRoleFieldName = customRoleFieldName;
    }

    public void updateRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }
}
