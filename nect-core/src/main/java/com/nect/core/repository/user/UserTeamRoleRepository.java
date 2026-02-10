package com.nect.core.repository.user;

import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTeamRoleRepository extends JpaRepository<UserTeamRole, Long> {

    boolean existsByProject_IdAndRoleFieldAndDeletedAtIsNull(
            Long projectId, RoleField roleField
    );

    boolean existsByProject_IdAndRoleFieldAndCustomRoleFieldNameIgnoreCaseAndDeletedAtIsNull(
            Long projectId, RoleField roleField, String customRoleFieldName
    );

    Optional<UserTeamRole> findByIdAndProject_IdAndDeletedAtIsNull(Long id, Long projectId);

    List<UserTeamRole> findAllByProject_IdAndDeletedAtIsNullOrderByIdAsc(Long projectId);

    List<UserTeamRole> findByProjectIdIn(List<Long> projectIds);
    Optional<UserTeamRole> findFirstByProjectIdAndRoleFieldAndCustomRoleFieldName(
            Long projectId,
            RoleField roleField,
            String customRoleFieldName
    );

    List<UserTeamRole> findAllByProjectId(Long projectId);
}
