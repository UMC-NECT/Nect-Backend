package com.nect.core.repository.user;

import com.nect.core.entity.user.UserPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolio, Long> {
    List<UserPortfolio> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);
}