package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

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

        @JsonProperty("field_ids")
        List<Long> fieldIds,

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
}
