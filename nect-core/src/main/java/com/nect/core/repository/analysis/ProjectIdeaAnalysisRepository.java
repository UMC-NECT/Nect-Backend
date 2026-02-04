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

    /**
     * 사용자별 분석 이력 조회 (최신순)
     */
    List<ProjectIdeaAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자별 분석 개수 조회
     */
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

    /*
    * // 1단계: 메인 엔티티 + 하나의 컬렉션만 FETCH JOIN
@Query("SELECT a FROM ProjectIdeaAnalysis a " +
       "LEFT JOIN FETCH a.weeklyRoadmaps " +  // 가장 중요한 것만
       "WHERE a.id = :analysisId AND a.userId = :userId")

// 2단계: 나머지는 BatchSize로 자동 로딩
    * */
    /**
     * 특정 분석서 상세 조회 (연관 엔티티 fetch join)
     */
    @Query("SELECT DISTINCT a FROM ProjectIdeaAnalysis a " +
            "LEFT JOIN FETCH a.weeklyRoadmaps w " +
            "WHERE a.id = :analysisId AND a.userId = :userId")
    Optional<ProjectIdeaAnalysis> findByIdAndUserIdWithDetails(
            @Param("analysisId") Long analysisId,
            @Param("userId") Long userId
    );

}