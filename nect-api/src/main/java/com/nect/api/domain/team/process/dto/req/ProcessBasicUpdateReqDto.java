package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;

import java.time.LocalDate;
import java.util.List;

public record ProcessBasicUpdateReqDto(
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
        List<Long> mentionUserIds
) {
    public ProcessBasicUpdateReqDto {
        // PATCH semantics:
        // null  => "변경 안 함"
        // []    => "전부 제거"
        if (roleFields != null) roleFields = roleFields.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (customFields != null) customFields = customFields.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (assigneeIds != null) assigneeIds = assigneeIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (mentionUserIds != null) mentionUserIds = mentionUserIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }
}
