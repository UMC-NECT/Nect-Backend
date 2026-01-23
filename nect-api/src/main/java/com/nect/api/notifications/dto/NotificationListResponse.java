package com.nect.api.notifications.dto;

import com.nect.core.entity.notifications.Notification;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> notifications
) {

    public static NotificationListResponse from(List<Notification> notifications) {
        return new NotificationListResponse(
                notifications.stream()
                        .map(NotificationResponse::from)
                        .toList()
        );
    }


}
