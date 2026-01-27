package com.nect.core.repository.team;

import com.nect.core.entity.team.Project;
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

}
