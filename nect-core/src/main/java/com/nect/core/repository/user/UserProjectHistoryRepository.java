package com.nect.core.repository.user;

import com.nect.core.entity.user.UserProjectHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectHistoryRepository extends JpaRepository<UserProjectHistory, Long> {
    List<UserProjectHistory> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);
}