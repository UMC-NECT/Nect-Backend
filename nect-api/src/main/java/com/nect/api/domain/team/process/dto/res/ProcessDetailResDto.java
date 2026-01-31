package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProcessDetailResDto(
        @JsonProperty("process_id")
        Long processId,

        @JsonProperty("process_title")
        String processTitle,

        @JsonProperty("process_content")
        String processContent,

        @JsonProperty("process_status")
        ProcessStatus processStatus,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("dead_line")
        LocalDate deadLine,

        @JsonProperty("status_order")
        Integer statusOrder,

        @JsonProperty("role_fields")
        List<RoleField> roleFields,

        @JsonProperty("custom_fields")
        List<String> customFields,

        @JsonProperty("assignees")
        List<AssigneeResDto> assignees,

        @JsonProperty("mention_user_ids")
        List<Long> mentionUserIds,

        List<FileResDto> files,

        List<LinkResDto> links,

        @JsonProperty("task_items")
        List<ProcessTaskItemResDto> taskItems,

        List<ProcessFeedbackCreateResDto> feedbacks,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt,

        @JsonProperty("deleted_at")
        LocalDateTime deletedAt
) {
    public ProcessDetailResDto {
        roleFields = (roleFields == null) ? List.of() : roleFields;
        customFields = (customFields == null) ? List.of() : customFields;
        assignees = (assignees == null) ? List.of() : assignees;
        mentionUserIds = (mentionUserIds == null) ? List.of() : mentionUserIds;
        files = (files == null) ? List.of() : files;
        links = (links == null) ? List.of() : links;
        taskItems = (taskItems == null) ? List.of() : taskItems;
        feedbacks = (feedbacks == null) ? List.of() : feedbacks;
    }
}
