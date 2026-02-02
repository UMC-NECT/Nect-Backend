package com.nect.core.repository.team.workspace;

import com.nect.core.entity.team.workspace.ProjectUserWorkDaily;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectUserWorkDailyRepository extends JpaRepository<ProjectUserWorkDaily, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select w
        from ProjectUserWorkDaily w
        where w.project.id = :projectId
          and w.user.userId = :userId
          and w.workDate = :workDate
    """)
    Optional<ProjectUserWorkDaily> findTodayForUpdate(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId,
            @Param("workDate") LocalDate workDate
    );

    @Query("""
        select w
        from ProjectUserWorkDaily w
        where w.project.id = :projectId
          and w.workDate = :workDate
    """)
    List<ProjectUserWorkDaily> findAllByProjectIdAndWorkDate(
            @Param("projectId") Long projectId,
            @Param("workDate") LocalDate workDate
    );
}
