package com.nect.api.domain.notifications.dto;

public class NotificationEnumResponse {

    public record EnumValueDto(
            String value,
            String label
    ) {}

    public record NotificationSearchFilterDto(
            String value,
            java.util.List<String> scopes
    ) {}

    public record NotificationTypeDto(
            String value,
            String mainMessageFormat,
            String contentMessageFormat,
            boolean hasContent
    ) {}
}