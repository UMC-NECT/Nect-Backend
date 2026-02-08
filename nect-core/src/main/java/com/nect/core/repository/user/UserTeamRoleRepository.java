package com.nect.core.repository.user;

import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTeamRoleRepository extends JpaRepository<UserTeamRole, Long> {

    boolean existsByProject_IdAndUser_UserIdAndRoleFieldAndDeletedAtIsNull(
            Long projectId, Long userId, RoleField roleField
    );

    boolean existsByProject_IdAndUser_UserIdAndRoleFieldAndCustomRoleFieldNameIgnoreCaseAndDeletedAtIsNull(
            Long projectId, Long userId, RoleField roleField, String customRoleFieldName
    );

    Optional<UserTeamRole> findByIdAndProject_IdAndDeletedAtIsNull(Long id, Long projectId);

}
