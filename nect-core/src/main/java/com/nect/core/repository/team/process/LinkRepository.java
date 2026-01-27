package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByIdAndProcessId(Long id, Long processId);
}
