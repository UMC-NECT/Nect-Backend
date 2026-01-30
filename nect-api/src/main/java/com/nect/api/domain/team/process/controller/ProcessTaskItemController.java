package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessTaskItemReorderReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessTaskItemUpsertReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemDeleteResDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemReorderResDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemResDto;
import com.nect.api.domain.team.process.service.ProcessTaskItemService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/processes/{processId}/task-items")
public class ProcessTaskItemController {
    private final ProcessTaskItemService taskItemService;

    // 업무 항목 생성
    @PostMapping
    public ApiResponse<ProcessTaskItemResDto> createTaskItem(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestBody ProcessTaskItemUpsertReqDto request
    ) {
        ProcessTaskItemResDto res = taskItemService.create(projectId, processId, request);
        return ApiResponse.ok(res);
    }

    // 업무 항목 수정
    @PatchMapping("/{taskItemId}")
    public ApiResponse<ProcessTaskItemResDto> updateTaskItem(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long taskItemId,
            @RequestBody ProcessTaskItemUpsertReqDto request
    ) {
        ProcessTaskItemResDto res = taskItemService.update(projectId, processId, taskItemId, request);
        return ApiResponse.ok(res);
    }

    // 압무 항목 삭제
    @DeleteMapping("/{taskItemId}")
    public ApiResponse<ProcessTaskItemDeleteResDto> deleteTaskItem(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long taskItemId
    ) {
        taskItemService.delete(projectId, processId, taskItemId);
        return ApiResponse.ok(new ProcessTaskItemDeleteResDto(taskItemId));
    }

    // 업무 위치 변경
    @PatchMapping("/reorder")
    public ApiResponse<ProcessTaskItemReorderResDto> reorderTaskItems(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestBody ProcessTaskItemReorderReqDto request
    ) {
        return ApiResponse.ok(taskItemService.reorder(projectId, processId, request));
    }
}
