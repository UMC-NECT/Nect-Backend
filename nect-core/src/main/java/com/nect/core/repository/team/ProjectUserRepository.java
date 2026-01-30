package com.nect.core.repository.team;

import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, Long> {

    interface ProjectHomeStat {
        Long getProjectId();
        Long getLeaderUserId();
        String getLeaderName();
        Long getLeaderFieldId();
        Long getActiveMemberCount();
    }

    Optional<ProjectUser> findByUserIdAndProject(Long userid, Project project);

    @Query("""
        SELECT u FROM User u 
        JOIN ProjectUser pu ON u.userId = pu.userId 
        WHERE pu.project.id = :projectId 
        AND pu.memberStatus = 'ACTIVE'
    """)
    List<User> findAllUsersByProjectId(@Param("projectId") Long projectId);

    @Query("""
        SELECT u FROM User u 
        JOIN ProjectUser pu ON u.userId = pu.userId 
        WHERE pu.project.id = :projectId 
        AND u.userId IN :userIds 
        AND pu.memberStatus = 'ACTIVE'
    """)
    List<User> findAllUsersByProjectIdAndUserIds(
            @Param("projectId") Long projectId,
            @Param("userIds") List<Long> userIds
    );

    @Query("SELECT COUNT(pu) > 0 FROM ProjectUser pu " +
            "WHERE pu.project.id = :projectId " +
            "AND pu.userId = :userId " +
            "AND pu.memberStatus = 'ACTIVE'")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);


    @Query("""
        SELECT
            pu.project.id as projectId,
            MAX(CASE WHEN pu.memberType = 'LEADER' AND pu.memberStatus = 'ACTIVE' THEN pu.userId END) as leaderUserId,
            MAX(CASE WHEN pu.memberType = 'LEADER' AND pu.memberStatus = 'ACTIVE' THEN u.name END) as leaderName,
            MAX(CASE WHEN pu.memberType = 'LEADER' AND pu.memberStatus = 'ACTIVE' THEN pu.fieldId END) as leaderFieldId,
            SUM(CASE WHEN pu.memberStatus = 'ACTIVE' THEN 1 ELSE 0 END) as activeMemberCount
        FROM ProjectUser pu
        LEFT JOIN User u ON u.userId = pu.userId
        WHERE pu.project.id IN :projectIds
        GROUP BY pu.project.id
    """)
    List<ProjectHomeStat> findProjectHomeStats(@Param("projectIds") List<Long> projectIds);

}
