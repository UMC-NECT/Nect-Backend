package com.nect.core.repository.analysis;

import com.nect.core.entity.analysis.ProjectIdeaAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectIdeaAnalysisRepository extends JpaRepository<ProjectIdeaAnalysis, Long> {


    List<ProjectIdeaAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);


    Long countByUserId(Long userId);

    List<ProjectIdeaAnalysis> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ProjectIdeaAnalysis> findFirstByUserIdOrderByCreatedAtAsc(Long userId);

    @Query("SELECT a FROM ProjectIdeaAnalysis a " +
            "WHERE a.userId = :userId " +
            "ORDER BY a.createdAt DESC")
    Page<ProjectIdeaAnalysis> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );


    @Query("SELECT DISTINCT a FROM ProjectIdeaAnalysis a " +
            "LEFT JOIN FETCH a.weeklyRoadmaps w " +
            "WHERE a.id = :analysisId AND a.userId = :userId")
    Optional<ProjectIdeaAnalysis> findByIdAndUserIdWithDetails(
            @Param("analysisId") Long analysisId,
            @Param("userId") Long userId
    );

}