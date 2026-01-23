package com.nect.core.repository;

import com.nect.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByNickname(String nickname);

	Optional<User> findBySocialProviderAndSocialId(String provider, String socialId);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);
}