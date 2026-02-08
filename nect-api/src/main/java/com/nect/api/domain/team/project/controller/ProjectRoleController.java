package com.nect.api.domain.team.project.controller;

import com.nect.api.domain.team.project.dto.ProjectPartsResDto;
import com.nect.api.domain.team.project.service.ProjectRoleQueryService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/roles")
public class ProjectRoleController {

    private final ProjectRoleQueryService projectRoleQueryService;

    // 작업실 전용 프로젝트 파트 목록 조회
    @GetMapping
    public ApiResponse<ProjectPartsResDto> readProjectParts(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(projectRoleQueryService.readProjectParts(projectId, userId));
    }
}
