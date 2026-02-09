package com.nect.core.repository.team;

import com.nect.core.entity.team.ProjectInterest;
import com.nect.core.entity.user.enums.InterestField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectInterestFieldRepository extends JpaRepository<ProjectInterest, Long> {
    Optional<ProjectInterest> findByProjectIdAndInterestField(Long projectId, InterestField interestField);
    List<ProjectInterest> findByProjectId(Long projectId);
}
