package com.nect.core.repository.user;

import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserInterest;
import com.nect.core.entity.user.enums.InterestField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);

    @Query("""
        SELECT u FROM UserInterest ui   
        LEFT JOIN FETCH User u
        WHERE ui.interestField = :interest
    """)
    List<User> findUsersByInterest(@Param("interest") InterestField interest, Pageable pageable);

}
