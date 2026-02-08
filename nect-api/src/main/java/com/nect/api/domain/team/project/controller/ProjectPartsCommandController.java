package com.nect.api.domain.team.project.controller;

import com.nect.api.domain.team.project.dto.ProjectPartCreateReqDto;
import com.nect.api.domain.team.project.dto.ProjectPartCreateResDto;
import com.nect.api.domain.team.project.dto.ProjectPartUpdateReqDto;
import com.nect.api.domain.team.project.dto.ProjectPartUpdateResDto;
import com.nect.api.domain.team.project.service.ProjectTeamCommandService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}")
public class ProjectPartsCommandController {

    private final ProjectTeamCommandService projectTeamCommandService;

    // 프로젝트 파트 추가 (작업실 팀 레인/ 드롭다운 연동)
    @PostMapping("/parts")
    public ApiResponse<ProjectPartCreateResDto> createProjectPart (
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProjectPartCreateReqDto req
    ) {
        return ApiResponse.ok(projectTeamCommandService.createProjectPart(projectId, userDetails.getUserId(), req));
    }

    // 프로젝트 커스텀 파트 이름 수정(기존 roleField는 수정 불가)
    @PatchMapping("/parts/{partId}")
    public ApiResponse<ProjectPartUpdateResDto> updateProjectPart(
            @PathVariable Long projectId,
            @PathVariable Long partId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProjectPartUpdateReqDto req
    ) {
        return ApiResponse.ok(
                projectTeamCommandService.updateProjectPart(projectId, partId, userDetails.getUserId(), req)
        );
    }

}
