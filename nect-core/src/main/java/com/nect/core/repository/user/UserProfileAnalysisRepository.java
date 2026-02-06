package com.nect.core.repository.user;

import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserProfileAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileAnalysisRepository extends JpaRepository<UserProfileAnalysis, Long> {
    Optional<UserProfileAnalysis> findByUser(User user);
}