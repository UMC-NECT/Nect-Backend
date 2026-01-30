package com.nect.core.repository.team;

import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
        SELECT p
        FROM Project p
        WHERE p.recruitmentStatus = :status
          AND NOT EXISTS (
              SELECT 1
              FROM ProjectUser pu
              WHERE pu.project = p
                AND pu.userId = :userId
        )
        ORDER BY p.createdAt DESC
    """)
    List<Project> findHomeProjects(@Param("userId") Long userId, @Param("status") RecruitmentStatus status, Pageable pageable);

    @Query("""
        SELECT p
        FROM Project p
        WHERE NOT EXISTS (
            SELECT 1
            FROM ProjectUser pu
            WHERE pu.project = p
              AND pu.userId = :userId
        )
    """)
    List<Project> findProjectsExcludingUser(@Param("userId") Long userId);

}
