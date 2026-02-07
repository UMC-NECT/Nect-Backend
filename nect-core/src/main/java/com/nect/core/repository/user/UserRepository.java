package com.nect.core.repository.user;

import com.nect.core.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	@Query("SELECT u FROM User u WHERE u.userId IN :userIds ORDER BY u.name")
	List<User> findAllByUserIdIn(@Param("userIds") List<Long> userIds);

	@Query("SELECT u FROM User u " +
			"WHERE u.userId IN :userIds " +
			"AND (u.nickname LIKE %:keyword% OR u.name LIKE %:keyword%) " +
			"ORDER BY u.name")
	List<User> findAllByUserIdInAndKeyword(
			@Param("userIds") List<Long> userIds,
			@Param("keyword") String keyword
	);
}
