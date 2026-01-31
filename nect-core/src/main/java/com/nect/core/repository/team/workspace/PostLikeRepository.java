package com.nect.core.repository.team.workspace;

import com.nect.core.entity.team.workspace.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserUserId(Long postId, Long userId);

    void deleteByPostIdAndUserUserId(Long postId, Long userId);
}
