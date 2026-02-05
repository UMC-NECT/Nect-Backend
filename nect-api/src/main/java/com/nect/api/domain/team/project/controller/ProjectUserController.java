package com.nect.api.domain.team.project.controller;

import com.nect.api.domain.team.project.dto.ProjectUserFieldReqDto;
import com.nect.api.domain.team.project.dto.ProjectUserFieldResDto;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.domain.team.project.dto.UserProjectDto;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project-users")
public class ProjectUserController {

    private final ProjectUserService projectUserService;

    @GetMapping("")
    public ApiResponse<List<UserProjectDto>> getProjectsByUser(
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return ApiResponse.ok(projectUserService.findProjectsByUser(user.getUserId()));
    }

    @PatchMapping("/{projectUserId}/field")
    public ApiResponse<ProjectUserFieldResDto> updateProjectUserField(
            @PathVariable @Positive Long projectUserId,
            @RequestBody @Valid ProjectUserFieldReqDto reqDto
    ) {
        return ApiResponse.ok(projectUserService.changeProjectUserFieldInProject(projectUserId, reqDto));
    }

    @PatchMapping("/{projectUserId}/kick")
    public ApiResponse<ProjectUserResDto> kickProjectUser(
            @PathVariable @Positive Long projectUserId
    ) {
        return ApiResponse.ok(projectUserService.kickProjectUser(projectUserId));
    }
}
