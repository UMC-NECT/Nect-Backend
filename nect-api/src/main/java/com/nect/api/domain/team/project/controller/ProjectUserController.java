package com.nect.api.domain.team.project.controller;

import com.nect.api.domain.team.project.dto.UserProjectDto;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
