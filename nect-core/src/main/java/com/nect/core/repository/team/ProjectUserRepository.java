package com.nect.core.repository.team;

import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, Long> {
    Optional<ProjectUser> findByUserIdAndProject(Long userid, Project project);
}
