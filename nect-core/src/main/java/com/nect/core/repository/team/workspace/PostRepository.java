package com.nect.core.repository.team.workspace;

import com.nect.core.entity.team.workspace.Post;
import com.nect.core.entity.team.workspace.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);

    // 공지: 개수 제한 X, 상단 노출용, 정렬은 서비스에서 Sort 주입
    @Query("""
        select p from Post p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.postType = com.nect.core.entity.team.workspace.enums.PostType.NOTICE
    """)
    List<Post> findAllNotices(@Param("projectId") Long projectId, Sort sort);

    // 자유글: 페이징 대상 (NOTICE 제외)
    @Query("""
        select p from Post p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.postType <> com.nect.core.entity.team.workspace.enums.PostType.NOTICE
    """)
    Page<Post> findFreePosts(@Param("projectId") Long projectId, Pageable pageable);
}
