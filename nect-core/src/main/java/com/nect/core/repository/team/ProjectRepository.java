package com.nect.core.repository.team;

import com.nect.core.entity.team.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}