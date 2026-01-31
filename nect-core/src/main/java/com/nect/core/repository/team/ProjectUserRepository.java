package com.nect.core.repository.team;

import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
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

    interface ProjectMemberInfo {
        Long getUserId();
        String getName();
        Long getFieldId();
        ProjectMemberType getMemberType();
        ProjectMemberStatus getMemberStatus();
    }

    interface UserRoleFieldsRow {
        Long getUserId();
        RoleField getRoleField();
        String getCustomRoleFieldName();
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


    @Query("""
        select
          pu.userId as userId,
          pu.roleField as roleField,
          pu.customRoleFieldName as customRoleFieldName
        from ProjectUser pu
        where pu.project.id = :projectId
          and pu.memberStatus = 'ACTIVE'
          and pu.userId in :userIds
    """)
    List<UserRoleFieldsRow> findActiveUserRoleFieldsByProjectIdAndUserIds(
            @Param("projectId") Long projectId,
            @Param("userIds") List<Long> userIds
    );

    long countByProject_IdAndUserIdIn(Long projectId, List<Long> userIds);

    @Query("""
        SELECT
            pu.userId as userId,
            u.name as name,
            pu.roleField as fieldId,
            pu.memberType as memberType,
            pu.memberStatus as memberStatus
        FROM ProjectUser pu
        JOIN User u ON u.userId = pu.userId
        WHERE pu.project.id = :projectId
        AND pu.memberStatus = 'ACTIVE'
    """)
    List<ProjectMemberInfo> findProjectMemberInfos(@Param("projectId") Long projectId);

    @Query("""
        SELECT COUNT(pu) > 0
        FROM ProjectUser pu
        WHERE pu.project.id = :projectId
          AND pu.userId = :userId
          AND pu.memberStatus = 'ACTIVE'
          AND pu.memberType = 'LEADER'
    """)
    boolean existsActiveLeader(@Param("projectId") Long projectId, @Param("userId") Long userId);


    interface MemberBoardRow {
        Long getUserId();
        String getName();
        String getNickname();
        RoleField getRoleField();
        String getCustomRoleFieldName();
        ProjectMemberType getMemberType();
    }

    @Query("""
        SELECT 
            pu.userId as userId,
            u.name as name,
            u.nickname as nickname,
            pu.roleField as roleField,
            pu.customRoleFieldName as customRoleFieldName,
            pu.memberType as memberType
        FROM ProjectUser pu
        JOIN User u ON u.userId = pu.userId
        WHERE pu.project.id = :projectId
          AND pu.memberStatus = 'ACTIVE'
    """)
    List<MemberBoardRow> findActiveMemberBoardRows(@Param("projectId") Long projectId);

}
