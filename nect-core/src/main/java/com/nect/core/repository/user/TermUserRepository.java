package com.nect.core.repository.user;

import com.nect.core.entity.user.TermUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermUserRepository extends JpaRepository<TermUser, Long> {
    Optional<TermUser> findByUserUserId(Long userId);
}
