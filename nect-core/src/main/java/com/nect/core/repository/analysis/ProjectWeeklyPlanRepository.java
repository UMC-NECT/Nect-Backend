package com.nect.core.repository.analysis;

import com.nect.core.entity.analysis.ProjectWeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectWeeklyPlanRepository extends JpaRepository<ProjectWeeklyPlan, Long> {
    List<ProjectWeeklyPlan> findByProjectIdOrderByWeekNumberAsc(Long projectId);
}