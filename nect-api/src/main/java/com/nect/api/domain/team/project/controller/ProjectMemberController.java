package com.nect.api.domain.team.project.controller;

import com.nect.api.domain.team.project.dto.ProjectUsersResDto;
import com.nect.api.domain.team.project.service.ProjectMemberQueryService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/users")
public class ProjectMemberController {

    private final ProjectMemberQueryService projectMemberQueryService;

    // 작업실용 프로젝트 유저 조회
    @GetMapping
    public ApiResponse<ProjectUsersResDto> readProjectUsers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(projectMemberQueryService.readProjectUsers(projectId, userId));
    }
}
