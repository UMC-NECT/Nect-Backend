package com.nect.core.repository.matching;

import com.nect.core.entity.matching.RecruitmentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentRequirementRepository extends JpaRepository<RecruitmentRequirement, Long> {
}
