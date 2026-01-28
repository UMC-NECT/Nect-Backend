package com.nect.core.repository.team.history;

import com.nect.core.entity.team.history.ProjectHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectHistoryRepository extends JpaRepository<ProjectHistory, Long> {
    // 무한스크롤/더보기를 할 거면 둘 다 씀
    // 최근 N개만 보여줄 거면 findLatest만 써도 됨
    @Query("""
        select h
        from ProjectHistory h
        where h.project = :projectId
        order by h.id desc
    """)
    List<ProjectHistory> findLatest(@Param("projectId") Long projectId, Pageable pageable);

    @Query("""
        select h
        from ProjectHistory h
        where h.project = :projectId
          and h.id < :cursorId
        order by h.id desc
    """)
    List<ProjectHistory> findLatestByCursor(@Param("projectId") Long projectId,
                                            @Param("cursorId") Long cursorId,
                                            Pageable pageable);
}