package com.nect.core.repository.user;

import com.nect.core.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findBySocialProviderAndSocialId(String provider, String socialId);

	boolean existsByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);

	boolean existsByNickname(String nickname);

	List<User> findByUserIdNot(Long userId, Pageable pageable);

	List<User> findByUserIdIn(List<Long> userIds);

}
