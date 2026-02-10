package com.nect.core.repository.team;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectTeamRoleRepository extends JpaRepository<ProjectTeamRole, Long> {
    List<ProjectTeamRole> findByProjectId(Long projectId);


    List<ProjectTeamRole> findByProjectIdIn(List<Long> projectIds);

    @Query("""
        select ptr
        from ProjectTeamRole ptr
        where ptr.project.id = :projectId
          and ptr.deletedAt is null
          order by ptr.id asc
    """)
    List<ProjectTeamRole> findAllActiveByProjectId(@Param("projectId") Long projectId);

    @Query("""
        select ptr.roleField as roleField,
               ptr.customRoleFieldName as customRoleFieldName
        from ProjectTeamRole ptr
        where ptr.project.id = :projectId
          and ptr.deletedAt is null
    """)
    List<TeamRoleRow> findActiveTeamRoleRowsByProjectId(@Param("projectId") Long projectId);

    interface TeamRoleRow {
        RoleField getRoleField();
        String getCustomRoleFieldName();
    }

    boolean existsByProject_IdAndRoleField(Long projectId, RoleField roleField);

    boolean existsByProject_IdAndRoleFieldAndCustomRoleFieldName(
            Long projectId,
            RoleField roleField,
            String customRoleFieldName
    );

    boolean existsByProject_IdAndDeletedAtIsNullAndRoleField(Long projectId, RoleField roleField);

    boolean existsByProject_IdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldName(
            Long projectId,
            RoleField roleField,
            String customRoleFieldName
    );


    Optional<ProjectTeamRole> findByIdAndDeletedAtIsNull(Long id);

    Optional<ProjectTeamRole> findByIdAndProject_IdAndDeletedAtIsNull(Long id, Long projectId);


    @Query("""
        select ptr
        from ProjectTeamRole ptr
        where ptr.project.id = :projectId
          and ptr.deletedAt is null
        order by
          case when ptr.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM then 1 else 0 end asc,
          ptr.createdAt asc,
          ptr.id asc
    """)
    List<ProjectTeamRole> findActiveOrderedForMissionProgress(@Param("projectId") Long projectId);

}
