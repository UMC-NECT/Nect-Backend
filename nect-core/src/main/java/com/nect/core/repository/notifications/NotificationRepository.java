package com.nect.core.repository.notifications;

import com.nect.core.entity.notifications.Notification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n FROM Notification n
        WHERE n.scope = :scope
            AND n.receiver = :user
          AND (:cursor IS NULL OR n.id < :cursor)
        ORDER BY n.id DESC
    """)
    List<Notification> findByScopeWithCursor(
            @Param("user") User user,
            @Param("scope") NotificationScope scope,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT n FROM Notification n
        WHERE n.scope IN :scopes
            AND n.receiver = :user
          AND (:cursor IS NULL OR n.id < :cursor)
        ORDER BY n.id DESC
    """)
    List<Notification> findByScopesWithCursor(
            @Param("user") User user,
            @Param("scopes") List<NotificationScope> scopes,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT n FROM Notification n
        WHERE n.scope IN :scopes
            AND n.receiver = :user
            AND n.project IN :projects
          AND (:cursor IS NULL OR n.id < :cursor)
        ORDER BY n.id DESC
    """)
    List<Notification> findByScopesAndProjectsWithCursor(
            @Param("user") User user,
            @Param("scopes") List<NotificationScope> scopes,
            @Param("projects") List<Project> projects,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

}
