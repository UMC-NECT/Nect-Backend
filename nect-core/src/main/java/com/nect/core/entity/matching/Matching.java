package com.nect.core.entity.matching;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
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

    // TODO: 타 도메인 구현 완료시 id 저장 방식 -> 엔티티 연관관계로 변경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_user_id", nullable = false)
    private Long requestUserId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "field_id", nullable = false)
    private Long fieldId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private MatchingRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status", nullable = false)
    private MatchingStatus matchingStatus;

    // TODO: 거절 사유 Enum Type으로 설정 예정
    @Column(name = "reject_reason", nullable = true)
    private String rejectReason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder
    public Matching(
            Long requestUserId,
            Long targetUserId,
            Long projectId,
            Long fieldId,
            MatchingRequestType requestType
            ){
        this.requestUserId = requestUserId;
        this.targetUserId = targetUserId;
        this.projectId = projectId;
        this.fieldId = fieldId;
        this.requestType = requestType;
        this.matchingStatus = MatchingStatus.PENDING;
    }

    @PrePersist
    public void calculateExpiration() {
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    public void changeStatus(MatchingStatus status) {
        this.matchingStatus = status;
    }
}
