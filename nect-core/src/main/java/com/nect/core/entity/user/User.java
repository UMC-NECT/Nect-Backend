package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_users_email"),
        @UniqueConstraint(columnNames = {"socialProvider", "socialId"}, name = "uk_users_social"),
        @UniqueConstraint(columnNames = "nickname", name = "uk_users_nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String nickname;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "birthdate")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    private Job job;

    // 온보딩 직종
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Goal firstGoal;

    @Column(name = "collaboration_style_planning")
    private Integer collaborationStylePlanning;

    @Column(name = "collaboration_style_logic")
    private Integer collaborationStyleLogic;

    @Column(name = "collaboration_style_leadership")
    private Integer collaborationStyleLeadership;

    @Column(name = "socialProvider")
    private String socialProvider;

    @Column(name = "socialId")
    private String socialId;

    @Builder.Default
    @Column(name = "isAutoLoginEnabled", nullable = false)
    private Boolean isAutoLoginEnabled = false;

    @Column(name = "profile_image_url")
    private String profileImageName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "core_competencies", columnDefinition = "TEXT")
    private String coreCompetencies;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status")
    private UserStatus userStatus;

    @Builder.Default
    @Column(name = "is_public_matching", nullable = false)
    private Boolean isPublicMatching = true;

    @Builder.Default
    @Column(name = "is_onboarding_completed", nullable = false)
    private Boolean isOnboardingCompleted = false;

    @Column(name = "career_duration")
    private String careerDuration;

    // 마이페이지 관심 직종
    @Column(name = "interested_job")
    private String interestedJob;

    // 마이페이지 관심 직무
    @Column(name = "interested_field")
    private String interestedField;

    public void updateAutoLoginEnabled(Boolean enabled) {
        this.isAutoLoginEnabled = enabled;
    }
}