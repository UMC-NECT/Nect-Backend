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

    @Query("""
        select r.project.id as projectId, sum(r.capacity) as capacitySum
        from Recruitment r
        where r.project.id in :projectIds
        group by r.project.id
    """)
    List<ProjectCapacityRow> sumCapacityByProjectIds(@Param("projectIds") List<Long> projectIds);

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

    interface ProjectCapacityRow {
        Long getProjectId();
        Integer getCapacitySum();
    }

    interface ProjectRoleCapacityRow {
        Long getProjectId();
        String getRoleName();
        Integer getCapacitySum();
    }
}
