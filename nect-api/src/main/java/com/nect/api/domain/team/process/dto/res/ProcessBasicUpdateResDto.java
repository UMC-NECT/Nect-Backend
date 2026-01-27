package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProcessBasicUpdateResDto(
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

        @JsonProperty("field_ids")
        List<Long> fieldIds,

        @JsonProperty("assignee_ids")
        List<Long> assigneeIds,

        @JsonProperty("mention_user_ids")
        List<Long> mentionUserIds,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
    public ProcessBasicUpdateResDto {
        fieldIds = (fieldIds == null) ? List.of() : fieldIds;
        assigneeIds = (assigneeIds == null) ? List.of() : assigneeIds;
        mentionUserIds = (mentionUserIds == null) ? List.of() : mentionUserIds;
    }
}
