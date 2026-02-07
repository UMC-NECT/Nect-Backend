package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_achievements")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAchievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_achievement_id")
    private Long userAchievementId;

    @ManyToOne
    @JoinColumn(name = "user_career_id", nullable = false)
    private UserCareer userCareer;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}