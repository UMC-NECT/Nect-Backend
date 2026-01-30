package com.nect.core.repository.matching;

import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    Optional<Recruitment> findRecruitmentByProjectAndFieldId(
            Project project,
            Long fieldId
    );

    @Query("""
        SELECT r
        FROM Recruitment r
        WHERE r.project.id IN :projectIds
    """)
    List<Recruitment> findAllByProjectIds(@Param("projectIds") List<Long> projectIds);
}
