package com.nect.core.entity.team;

import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
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

    @Column(name = "field_id")
    private Long fieldId;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private ProjectMemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false)
    private ProjectMemberStatus memberStatus;

    @Builder
    private ProjectUser(Project project, Long userId, Long fieldId, ProjectMemberType memberType, ProjectMemberStatus memberStatus) {
        this.project = project;
        this.userId = userId;
        this.fieldId = fieldId;
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
}
