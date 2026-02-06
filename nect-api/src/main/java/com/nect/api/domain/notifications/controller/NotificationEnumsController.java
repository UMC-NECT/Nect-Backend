package com.nect.api.domain.notifications.controller;

import com.nect.api.domain.notifications.dto.NotificationEnumResponse.EnumValueDto;
import com.nect.api.domain.notifications.dto.NotificationEnumResponse.NotificationSearchFilterDto;
import com.nect.api.domain.notifications.dto.NotificationEnumResponse.NotificationTypeDto;
import com.nect.api.domain.notifications.enums.code.NotificationSearchFilter;
import com.nect.api.global.response.ApiResponse;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/enums/notifications")
public class NotificationEnumsController {

    @GetMapping("/classifications")
    public ApiResponse<List<EnumValueDto>> getClassifications() {
        List<EnumValueDto> response = Arrays.stream(NotificationClassification.values())
                .map(value -> new EnumValueDto(value.name(), value.getClassifyKr()))
                .toList();
        return ApiResponse.ok(response);
    }

    @GetMapping("/scopes")
    public ApiResponse<List<EnumValueDto>> getScopes() {
        List<EnumValueDto> response = Arrays.stream(NotificationScope.values())
                .map(value -> new EnumValueDto(value.name(), value.getEventName()))
                .toList();
        return ApiResponse.ok(response);
    }

    @GetMapping("/filters")
    public ApiResponse<List<NotificationSearchFilterDto>> getFilters() {
        List<NotificationSearchFilterDto> response = Arrays.stream(NotificationSearchFilter.values())
                .map(value -> new NotificationSearchFilterDto(
                        value.name(),
                        value.getScopes().stream().map(Enum::name).toList()
                ))
                .toList();
        return ApiResponse.ok(response);
    }

    @GetMapping("/types/matching-rejected")
    public ApiResponse<NotificationTypeDto> getMatchingRejectedType() {
        NotificationType type = NotificationType.MATCHING_REJECTED;
        NotificationTypeDto response = new NotificationTypeDto(
                type.name(),
                type.getMainMessageFormat(),
                type.getContentMessageFormat(),
                type.hasContent()
        );
        return ApiResponse.ok(response);
    }



}
