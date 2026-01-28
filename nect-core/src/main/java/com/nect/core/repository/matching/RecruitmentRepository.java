package com.nect.core.repository.matching;

import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    Optional<Recruitment> findRecruitmentByProjectAndFieldId(
            Project project,
            Long fieldId
    );
}
