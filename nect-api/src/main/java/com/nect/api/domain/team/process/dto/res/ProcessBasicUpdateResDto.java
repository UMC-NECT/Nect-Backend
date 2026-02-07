package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;

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

        @JsonProperty("role_fields")
        List<RoleField> roleFields,

        @JsonProperty("custom_fields")
        List<String> customFields,

        @JsonProperty("assignee_ids")
        List<Long> assigneeIds,

        @JsonProperty("mention_user_ids")
        List<Long> mentionUserIds,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt,

        @JsonProperty("writer")
        WriterDto writer
) {
    public ProcessBasicUpdateResDto {
        roleFields = (roleFields == null) ? List.of() : roleFields;
        customFields = (customFields == null) ? List.of() : customFields;
        assigneeIds = (assigneeIds == null) ? List.of() : assigneeIds;
        mentionUserIds = (mentionUserIds == null) ? List.of() : mentionUserIds;
    }

    public record WriterDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("name")
            String name,

            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("role_field")
            RoleField roleField,

            @JsonProperty("custom_field_name")
            String customFieldName
    ) {}
}
