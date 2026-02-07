package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.api.domain.team.process.enums.AttachmentType;
import com.nect.core.entity.team.enums.FileExt;
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

        @JsonProperty("task_items")
        List<ProcessTaskItemResDto> taskItems,

        @JsonProperty("feedbacks")
        List<ProcessFeedbackCreateResDto> feedbacks,

        @JsonProperty("attachments")
        List<AttachmentDto> attachments,

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
        taskItems = (taskItems == null) ? List.of() : taskItems;
        feedbacks = (feedbacks == null) ? List.of() : feedbacks;
        attachments = (attachments == null) ? List.of() : attachments;
    }

    public record AttachmentDto(
            @JsonProperty("type")
            AttachmentType type,

            // 공통 식별자(파일이면 file_id, 링크면 link_id)
            @JsonProperty("id")
            Long id,

            @JsonProperty("created_at")
            LocalDateTime createdAt,

            // LINK 전용
            @JsonProperty("title")
            String title,

            @JsonProperty("url")
            String url,

            // FILE 전용
            @JsonProperty("file_name")
            String fileName,

            @JsonProperty("file_url")
            String fileUrl,

            @JsonProperty("file_type")
            FileExt fileType,

            @JsonProperty("file_size")
            Long fileSize
    ) {}
}
