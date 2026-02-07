package com.nect.core.repository.user;

import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserRole;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser(User user);

    interface UserRoleFieldRow {
        Long getUserId();
        RoleField getRoleField();
    }

    @Query("""
        select ur.user.userId as userId, ur.roleField as roleField
        from UserRole ur
        where ur.user in :users
    """)
    List<UserRoleFieldRow> findUserRoleFieldsByUsers(@Param("users") List<User> users);



}
