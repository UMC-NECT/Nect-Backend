package com.nect.core.repository.user;

import com.nect.core.entity.user.UserSkill;
import com.nect.core.entity.user.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserUserId(Long userId);
    List<UserSkill> findByUserUserIdAndSkillCategory(Long userId, SkillCategory skillCategory);
    void deleteByUserUserId(Long userId);
}
