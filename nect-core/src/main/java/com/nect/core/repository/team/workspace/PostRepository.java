package com.nect.core.repository.team.workspace;

import com.nect.core.entity.team.workspace.Post;
import com.nect.core.entity.team.workspace.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);

    @Query("""
        select p
        from Post p
        where p.project.id = :projectId
          and p.deletedAt is null
          and (:type is null or p.postType = :type)
        """)
    Page<Post> findPosts(
            @Param("projectId") Long projectId,
            @Param("type") PostType type,
            Pageable pageable
    );
}
