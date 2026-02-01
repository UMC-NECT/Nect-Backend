package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.facade.WorkTimerFacade;
import com.nect.api.domain.team.workspace.service.WorkTimerService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/boards/work")
public class WorkTimerController {

    private final WorkTimerFacade workTimerFacade;

    // 작업 시작
    @PostMapping("/start")
    public ApiResponse<Void> start(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        workTimerFacade.start(projectId, userId);
        return ApiResponse.ok(null);
    }

    // 작업 정지
    @PostMapping("/stop")
    public ApiResponse<Void> stop(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        workTimerFacade.stop(projectId, userId);
        return ApiResponse.ok(null);
    }
}
