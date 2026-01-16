package com.nect.api.domain.team.process.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.process.enums.ProcessStatus;

import java.time.LocalDate;
import java.util.List;

public record ProcessCreateReqDTO(
        // TODO : @Valid 나중에 필수값 확정되면 사용하기

        @JsonProperty("process_title")
        String processTitle,

        @JsonProperty("process_content")
        String processContent,

        @JsonProperty("process_status")
        ProcessStatus processStatus,

        @JsonProperty("assignee_ids")
        List<Long> assigneeIds,

        @JsonProperty("field_ids")
        List<Long> fieldIds,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("dead_line")
        LocalDate deadLine,

        @JsonProperty("mentions")
        List<Long> mentions,

        @JsonProperty("file_ids")
        List<Long> fileIds,

        @JsonProperty("links")
        List<String> links,

        @JsonProperty("task_items")
        List<ProcessTaskItemReqDTO> taskItems


//        TODO : 일단은 null -> 프로세스 생성할 때 초기 피드백도 같이 등록할 수 있으면 사용
//        List<ProcessFeedbackCreateReqDTO> feedbacks
) {}
