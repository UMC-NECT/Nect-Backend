package com.nect.core.repository.user;

import com.nect.core.entity.user.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserCareerUserCareerId(Long userCareerId);
    void deleteByUserCareerUserCareerId(Long userCareerId);
}