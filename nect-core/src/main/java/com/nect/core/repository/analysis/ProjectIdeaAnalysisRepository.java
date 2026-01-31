package com.nect.core.repository.analysis;

import com.nect.core.entity.analysis.ProjectIdeaAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}