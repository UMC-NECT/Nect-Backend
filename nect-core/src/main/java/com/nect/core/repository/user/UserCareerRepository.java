package com.nect.core.repository.user;

import com.nect.core.entity.user.UserCareer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCareerRepository extends JpaRepository<UserCareer, Long> {
    List<UserCareer> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);
}