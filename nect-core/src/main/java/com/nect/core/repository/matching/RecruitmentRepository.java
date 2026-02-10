package com.nect.core.repository.matching;

import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    Optional<Recruitment> findRecruitmentByProjectAndField(
            Project project,
            RoleField field
    );

    List<Recruitment> findByProject(Project project);

    List<Recruitment> findAllByProject_IdIn(@Param("projectIds") List<Long> projectIds);

    @Query("""
        select r
        from Recruitment r
        where r.project = :project
            and r.capacity > 0
    """)
    List<Recruitment> findOpenFieldsByProject(
            @Param("project") Project project
    );

    Optional<Recruitment> findByIdAndProject(Long recruitmentId, Project project);

}
