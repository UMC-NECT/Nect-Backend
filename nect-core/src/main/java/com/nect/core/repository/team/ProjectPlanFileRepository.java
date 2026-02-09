package com.nect.core.repository.team;

import com.nect.core.entity.team.ProjectPlanFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectPlanFileRepository extends JpaRepository<ProjectPlanFile, Long> {
    Optional<ProjectPlanFile> findByIdAndProjectId(Long id, Long projectId);
    List<ProjectPlanFile> findByProjectId(Long projectId);
}
