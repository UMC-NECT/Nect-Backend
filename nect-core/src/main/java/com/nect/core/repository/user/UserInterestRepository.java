package com.nect.core.repository.user;

import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserInterest;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.enums.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);

    @Query("""
        SELECT ui.user
        FROM UserInterest ui
        JOIN ui.user u
        WHERE ui.interestField = :interest
    """)
    List<User> findUsersByInterest(@Param("interest") InterestField interest, Pageable pageable);

    @Query("""
        SELECT ui.user
        FROM UserInterest ui
        JOIN ui.user u
        WHERE ui.interestField = :interest
          AND u.role = :role
          AND (:userId IS NULL OR u.userId <> :userId)
    """)
    List<User> findUsersByInterestAndRoleExcludingUser(
            @Param("interest") InterestField interest,
            @Param("role") Role role,
            @Param("userId") Long userId,
            Pageable pageable
    );

}
