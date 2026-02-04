package com.nect.api.domain.notifications.dto;

public class NotificationEnumResponse {

    public record EnumValueDto(
            String value,
            String label
    ) {}

    public record NotificationTypeDto(
            String value,
            String mainMessageFormat,
            String contentMessageFormat,
            boolean hasContent
    ) {}
}