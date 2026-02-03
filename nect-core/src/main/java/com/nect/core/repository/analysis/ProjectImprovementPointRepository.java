package com.nect.core.repository.analysis;

import com.nect.core.entity.analysis.ProjectImprovementPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectImprovementPointRepository extends JpaRepository<ProjectImprovementPoint, Long> {
    List<ProjectImprovementPoint> findByProjectIdOrderByPointOrderAsc(Long projectId);
}