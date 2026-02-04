package com.nect.core.repository.analysis;

import com.nect.core.entity.analysis.ProjectWeeklyTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectWeeklyTaskRepository extends JpaRepository<ProjectWeeklyTask, Long> {
    List<ProjectWeeklyTask> findByWeeklyPlanId(Long weeklyPlanId);
}