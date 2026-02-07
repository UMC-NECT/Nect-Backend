package com.nect.api.domain.team.project.controller;

import com.nect.api.domain.team.project.dto.ProjectPartsResDto;
import com.nect.api.domain.team.project.dto.ProjectUsersResDto;
import com.nect.api.domain.team.project.service.ProjectTeamQueryService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}")
public class ProjectPartsController {

    private final ProjectTeamQueryService projectTeamQueryService;

    // 팀 파트 조회 (드롭다운)
    @GetMapping("/parts")
    public ApiResponse<ProjectPartsResDto> readProjectParts(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(
                projectTeamQueryService.readProjectParts(projectId, userDetails.getUserId())
        );
    }

    // 프로젝트 전체 인원 조회
    @GetMapping("/users")
    public ApiResponse<ProjectUsersResDto> readProjectUsers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(
                projectTeamQueryService.readProjectUsers(projectId, userDetails.getUserId())
        );
    }
}
