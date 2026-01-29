package com.nect.core.repository.team.history;

import com.nect.core.entity.team.history.ProjectHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectHistoryRepository extends JpaRepository<ProjectHistory, Long> {
    @Query("""
        select h
        from ProjectHistory h
        where h.project.id = :projectId
        order by h.id desc
    """)
    List<ProjectHistory> findLatest(@Param("projectId") Long projectId, Pageable pageable);

    @Query("""
        select h
        from ProjectHistory h
        where h.project.id = :projectId
          and h.id < :cursorId
        order by h.id desc
    """)
    List<ProjectHistory> findLatestByCursor(
            @Param("projectId") Long projectId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}