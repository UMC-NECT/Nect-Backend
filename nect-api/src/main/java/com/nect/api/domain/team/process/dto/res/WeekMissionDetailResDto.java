package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WeekMissionDetailResDto(
        @JsonProperty("process_id")
        Long processId,

        @JsonProperty("mission_number")
        Integer missionNumber,

        @JsonProperty("title")
        String title,

        @JsonProperty("content")
        String content,

        @JsonProperty("status")
        ProcessStatus status,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("dead_line")
        LocalDate deadLine,

        @JsonProperty("assignee")
        AssigneeDto assignee,

        @JsonProperty("attachments")
        List<AttachmentDto> attachments,

        @JsonProperty("task_groups")
        List<TaskGroupResDto> taskGroups,

        @JsonProperty("task_items")
        List<ProcessTaskItemResDto> taskItems,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
    public record AssigneeDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("name")
            String name,

            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {}

    public record TaskGroupResDto(
            @JsonProperty("role_field")
            RoleField roleField,

            @JsonProperty("custom_field_name")
            String customFieldName,

            @JsonProperty("items")
            List<ProcessTaskItemResDto> items
    ) {}
}
