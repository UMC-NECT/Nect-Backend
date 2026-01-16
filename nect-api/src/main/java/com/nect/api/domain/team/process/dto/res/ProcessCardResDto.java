package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record ProcessCardResDto(
        @JsonProperty("process_id")
        Long processId,

        @JsonProperty("process_status")
        String processStatus,

        // 파트(담당 분야)
        String field,

        String title,

        @JsonProperty("complete_check_list")
        Integer completeCheckList,

        @JsonProperty("whole_check_list")
        Integer wholeCheckList,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("dead_line")
        LocalDate deadLine,

        @JsonProperty("left_day")
        Integer leftDay,

        @JsonProperty("assignee")
        List<AssigneeResDto> assignee
) {}
