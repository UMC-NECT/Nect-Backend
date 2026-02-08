package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ProcessCreateReqDto(

        @NotBlank
        @JsonProperty("process_title")
        String processTitle,

        @JsonProperty("process_content")
        String processContent,

        @JsonProperty("process_status")
        ProcessStatus processStatus,

        @JsonProperty("assignee_ids")
        List<Long> assigneeIds,

        @JsonProperty("role_fields")
        List<RoleField> roleFields,

        @JsonProperty("custom_field_name")
        String customFieldName,

        @JsonProperty("mission_number")
        Integer missionNumber,

        @NotNull
        @JsonProperty("start_date")
        LocalDate startDate,

        @NotNull
        @JsonProperty("dead_line")
        LocalDate deadLine,

        @JsonProperty("mention_user_ids")
        List<Long> mentionUserIds,

        @JsonProperty("file_ids")
        List<Long> fileIds,

        @JsonProperty("links")
        @Valid
        List<ProcessLinkItemReqDto> links,

        @NotNull
        @Size(min = 1)
        @JsonProperty("task_items")
        List<ProcessTaskItemReqDto> taskItems

//        List<ProcessFeedbackCreateReqDTO> feedbacks
) {
    public ProcessCreateReqDto {
        assigneeIds = (assigneeIds == null) ? List.of() : assigneeIds;
        roleFields = (roleFields == null) ? List.of() : roleFields;
        mentionUserIds = (mentionUserIds == null) ? List.of() : mentionUserIds;
        fileIds = (fileIds == null) ? List.of() : fileIds;
        links = (links == null) ? List.of() : links;
        taskItems = (taskItems == null) ? List.of() : taskItems;
    }

    public record ProcessLinkItemReqDto(
            @NotBlank
            @JsonProperty("title")
            String title,

            @NotBlank
            @JsonProperty("url")
            String url
    ) {}
}
