package com.nect.core.entity.team;

import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "project_user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_user_project_user",
                        columnNames = {"project_id", "user_id"}
                )
        }
)
public class ProjectUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: User 엔티티 연결되면 연관관계로 변경
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id", nullable = false)
    // private User user;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false, length = 50)
    private RoleField roleField;

    // CUSTOM 대응 필드
    @Column(name = "custom_role_field_name", length = 50)
    private String customRoleFieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private ProjectMemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false)
    private ProjectMemberStatus memberStatus;

    @Builder
    private ProjectUser(Project project, Long userId, RoleField roleField, String customRoleFieldName, ProjectMemberType memberType, ProjectMemberStatus memberStatus) {
        if (roleField == null) {
            throw new IllegalArgumentException("roleField는 null일 수 없습니다.");
        }

        if (roleField == RoleField.CUSTOM && (customRoleFieldName == null || customRoleFieldName.isBlank())) {
            throw new IllegalArgumentException("CUSTOM이면 customRoleFieldName(직접입력)이 필수입니다.");
        }

        this.project = project;
        this.userId = userId;
        this.roleField = roleField;
        this.customRoleFieldName = (roleField == RoleField.CUSTOM) ? customRoleFieldName : null;
        this.memberType = (memberType != null) ? memberType : ProjectMemberType.MEMBER;
        this.memberStatus = (memberStatus != null) ? memberStatus : ProjectMemberStatus.ACTIVE;
    }

    void setProject(Project project) {
        this.project = project;
    }

    public void activate() {
        this.memberStatus = ProjectMemberStatus.ACTIVE;
    }

    public void kick() {
        this.memberStatus = ProjectMemberStatus.KICKED;
    }

    public void changeField(RoleField roleField, String customRoleFieldName){
        this.roleField = roleField;
        this.customRoleFieldName = customRoleFieldName;
    }

    public void changeType(ProjectMemberType memberType){
        this.memberType = memberType;
    }
}
