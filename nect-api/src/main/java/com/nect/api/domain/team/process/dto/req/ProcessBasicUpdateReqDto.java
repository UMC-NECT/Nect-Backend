package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

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

        @JsonProperty("field_ids")
        List<Long> fieldIds,

        @JsonProperty("assignee_ids")
        List<Long> assigneeIds,

        @JsonProperty("mention_user_ids")
        List<Long> mentionUserIds
) {}
