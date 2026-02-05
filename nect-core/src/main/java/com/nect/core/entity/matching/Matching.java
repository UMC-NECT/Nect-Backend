package com.nect.core.entity.matching;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.matching.enums.MatchingRejectReason;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_user_id", nullable =false)
    private User requestUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable =false)
    private User targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable =false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "field", nullable = false)
    private RoleField field;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private MatchingRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status", nullable = false)
    private MatchingStatus matchingStatus;

    @Column(name = "reject_reason", nullable = true)
    private MatchingRejectReason rejectReason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "custom_field", nullable = true)
    private String customField;

    @Builder
    public Matching(
            User requestUser,
            User targetUser,
            Project project,
            RoleField field,
            MatchingRequestType requestType,
            String customField
            ){
        this.requestUser = requestUser;
        this.targetUser = targetUser;
        this.project = project;
        this.field = field;
        this.requestType = requestType;
        this.customField = customField;
        this.matchingStatus = MatchingStatus.PENDING;
    }

    @PrePersist
    public void calculateExpiration() {
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    public void changeStatus(MatchingStatus status) {
        this.matchingStatus = status;
    }

    public void setRejectReason(MatchingRejectReason rejectReason) {
        this.rejectReason = rejectReason;
    }
}
