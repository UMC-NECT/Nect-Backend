package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;

import java.time.LocalDate;
import java.util.List;

public record ProcessCreateReqDto(
        // TODO : @Valid 나중에 필수값 확정되면 사용하기

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

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("dead_line")
        LocalDate deadLine,

        @JsonProperty("mention_user_ids")
        List<Long> mentionUserIds,

        @JsonProperty("file_ids")
        List<Long> fileIds,

        @JsonProperty("links")
        List<String> links,

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
}
