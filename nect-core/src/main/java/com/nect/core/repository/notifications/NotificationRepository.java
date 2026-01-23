package com.nect.core.repository.notifications;

import com.nect.core.entity.notifications.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
