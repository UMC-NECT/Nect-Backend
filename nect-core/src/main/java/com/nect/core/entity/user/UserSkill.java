package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.Skill;
import com.nect.core.entity.user.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_skills")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSkill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_skill_id")
    private Long userSkillId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_category", nullable = false)
    private SkillCategory skillCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill", nullable = false)
    private Skill skill;

    @Column(name = "custom_skill_name")
    private String customSkillName;
}
