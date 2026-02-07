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

    Optional<ProjectUser> findByUserIdAndProject(Long userId, Project project);

    @Query("""
    SELECT pu.userId 
    FROM ProjectUser pu 
    WHERE pu.project.id = :projectId 
      AND pu.userId != :currentUserId 
      AND pu.memberStatus = 'ACTIVE'
""")
    List<Long> findUserIdsByProjectIdExcludingUser(
            @Param("projectId") Long projectId,
            @Param("currentUserId") Long currentUserId
    );

    @Query("""
        select pu
        from ProjectUser pu
        where pu.userId = :userId
            and pu.memberStatus = :memberStatus
    """)
    List<ProjectUser> findByUserIdAndProjectMemberStatus(
            @Param("userId") Long userId,
            @Param("memberStatus") ProjectMemberStatus memberStatus
    );


    @Query("""
        SELECT u FROM User u 
        JOIN ProjectUser pu ON u.userId = pu.userId 
        WHERE pu.project.id = :projectId 
        AND pu.memberStatus = 'ACTIVE'
    """)
    List<User> findAllUsersByProjectId(@Param("projectId") Long projectId);


    @Query("""
        SELECT pu.project
        FROM ProjectUser pu
        WHERE pu.userId = :userId
          AND pu.memberStatus = 'ACTIVE'
    """)
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);

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

    @Query("""
        SELECT u FROM User u 
        JOIN ProjectUser pu ON u.userId = pu.userId 
        WHERE pu.project.id = :projectId 
        AND u.userId IN :userIds 
        AND pu.memberStatus = com.nect.core.entity.team.enums.ProjectMemberStatus.ACTIVE
    """)
    List<User> findActiveUsersByProjectIdAndUserIds(
                                                      @Param("projectId") Long projectId,
                                                      @Param("userIds") List<Long> userIds
    );




    @Query("SELECT COUNT(pu) > 0 FROM ProjectUser pu " +
            "WHERE pu.project.id = :projectId " +
            "AND pu.userId = :userId " +
            "AND pu.memberStatus = 'ACTIVE'")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

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
        select pu.project.id as projectId, pu.userId as leaderUserId
        from ProjectUser pu
        where pu.project.id in :projectIds
          and pu.memberType = 'LEADER'
    """)
    List<ProjectLeaderRow> findLeadersByProjectIds(@Param("projectIds") List<Long> projectIds);

    @Query("""
        select pu.project.id as projectId, count(pu) as activeCount
        from ProjectUser pu
        where pu.project.id in :projectIds
          and pu.memberStatus = 'ACTIVE'
        group by pu.project.id
    """)
    List<ProjectActiveCountRow> countActiveMembersByProjectIds(@Param("projectIds") List<Long> projectIds);

    List<ProjectUser> findByProject(Project Project);

    @Query("""
        SELECT COUNT(pu) > 0
        FROM ProjectUser pu
        WHERE pu.project.id = :projectId
          AND pu.userId = :userId
          AND pu.memberStatus = 'ACTIVE'
          AND pu.memberType = 'LEADER'
    """)
    boolean existsActiveLeader(@Param("projectId") Long projectId, @Param("userId") Long userId);




    @Query("""
        SELECT 
            pu.userId as userId,
            u.name as name,
            u.nickname as nickname,
            u.profileImageName as profileImageName,
            u.bio as bio,
            pu.roleField as roleField,
            pu.customRoleFieldName as customRoleFieldName,
            pu.memberType as memberType
        FROM ProjectUser pu
        JOIN User u ON u.userId = pu.userId
        WHERE pu.project.id = :projectId
          AND pu.memberStatus = 'ACTIVE'
    """)
    List<MemberBoardRow> findActiveMemberBoardRows(@Param("projectId") Long projectId);

    @Query("""
        select pu.userId
        from ProjectUser pu
        where pu.project = :project
            and pu.memberType = com.nect.core.entity.team.enums.ProjectMemberType.LEADER
    """)
    Long findLeaderByProject(@Param("project") Project project);

    @Query("""
        select pu.project
        from ProjectUser pu
        where pu.userId = :userId
            and pu.memberType = com.nect.core.entity.team.enums.ProjectMemberType.LEADER
    """)
    List<Project> findProjectsAsLeader(@Param("userId") Long userId);

    @Query("""
        select count(pu)
        from ProjectUser pu
        where pu.memberStatus = :status 
            and pu.project = :project
    """)
    long countProjectUserByMemberStatusAndProject(@Param("status")ProjectMemberStatus status, @Param("project") Project project);

    boolean existsByProjectIdAndUserIdAndMemberStatus(Long projectId, Long userId, ProjectMemberStatus memberStatus);

    @Query("""
        SELECT pu FROM ProjectUser pu 
        WHERE pu.project.id = :projectId 
        AND pu.userId IN :userIds 
        AND pu.memberStatus = 'ACTIVE'
    """)
    List<ProjectUser> findAllActiveProjectMembers(
            @Param("projectId") Long projectId,
            @Param("userIds") List<Long> userIds
    );

    @Query("""
        select count(pu) > 0
        from ProjectUser pu
        where pu.project = :project
            and pu.memberType = com.nect.core.entity.team.enums.ProjectMemberType.LEAD
            and pu.memberStatus = com.nect.core.entity.team.enums.ProjectMemberStatus.ACTIVE
            and pu.roleField = :roleField
            and (
                :roleField <> com.nect.core.entity.user.enums.RoleField.CUSTOM
                or pu.customRoleFieldName = :customRoleFieldName
                )
    """)
    boolean existsActiveLeadInProject(
            @Param("project") Project project,
            @Param("roleField") RoleField roleField,
            @Param("customRoleFieldName") String customRoleFieldName
    );


    Optional<ProjectUser> findByProjectIdAndMemberType(Long projectId, ProjectMemberType memberType);

    boolean existsByProjectIdAndUserIdAndMemberTypeAndMemberStatus(Long projectId, Long userId, ProjectMemberType projectMemberType, ProjectMemberStatus projectMemberStatus);

    @Query("""
        select
          u.userId as userId,
          u.nickname as nickname,
          u.profileImageName as profileImageName
        from ProjectUser pu
        join User u
          on u.userId = pu.userId
        where pu.project.id = :projectId
          and pu.memberStatus = com.nect.core.entity.team.enums.ProjectMemberStatus.ACTIVE
          and pu.memberType = com.nect.core.entity.team.enums.ProjectMemberType.LEADER
    """)
    Optional<ProjectLeaderProfileRow> findActiveLeaderProfile(@Param("projectId") Long projectId);

    interface UserFieldIdsRow {
        Long getUserId();
        Long getFieldId();
    }

    interface ProjectLeaderRow {
        Long getProjectId();
        Long getLeaderUserId();
    }

    interface ProjectActiveCountRow {
        Long getProjectId();
        Long getActiveCount();
    }

    interface UserRoleFieldsRow {
        Long getUserId();
        RoleField getRoleField();
        String getCustomRoleFieldName();
    }

    interface MemberBoardRow {
        Long getUserId();
        String getName();
        String getNickname();
        String getProfileImageName();
        String getBio();
        RoleField getRoleField();
        String getCustomRoleFieldName();
        ProjectMemberType getMemberType();
    }

    interface ProjectLeaderProfileRow {
        Long getUserId();
        String getNickname();
        String getProfileImageUrl();
    }


}
