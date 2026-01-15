package com.nect.core.entity.matching;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching extends BaseEntity {

    @Builder
    public Matching(
            Long requestUserId,
            Long targetUserId,
            Long projectId,
            MatchingRequestType requestType
            ){
        this.requestUserId = requestUserId;
        this.targetUserId = targetUserId;
        this.projectId = projectId;
        this.requestType = requestType;
        this.matchingStatus = MatchingStatus.PENDING;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_user_id", nullable = false)
    private Long requestUserId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private MatchingRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status", nullable = false)
    private MatchingStatus matchingStatus;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    public void calculateExpiration() {
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }
}
