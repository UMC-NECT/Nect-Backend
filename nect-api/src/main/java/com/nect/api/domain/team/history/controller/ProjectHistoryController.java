package com.nect.api.domain.team.history.controller;

import com.nect.api.domain.team.history.dto.res.ProjectHistoryListResDto;
import com.nect.api.domain.team.history.service.ProjectHistoryService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/histories")
public class ProjectHistoryController {

    private final ProjectHistoryService historyService;

    // 최근 10개 조회 + cursor 기반 다음 10개 조회
    @GetMapping
    public ApiResponse<ProjectHistoryListResDto> getHistories(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long cursor
    ) {
        return ApiResponse.ok(historyService.getHistories(projectId, cursor));
    }
}