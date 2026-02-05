package com.nect.core.repository.analysis;
import com.nect.core.entity.team.process.ProjectTeamRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTeamRoleRepository extends JpaRepository<ProjectTeamRole, Long> {
    List<ProjectTeamRole> findByProjectId(Long projectId);
}
