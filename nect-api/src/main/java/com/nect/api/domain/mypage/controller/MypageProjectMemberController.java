package com.nect.api.domain.mypage.controller;

import com.nect.api.domain.team.project.dto.ProjectUsersResDto;
import com.nect.api.domain.team.project.service.ProjectMemberQueryService;
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
@RequestMapping("/api/v1/mypage/projects/{projectId}/users")
public class MypageProjectMemberController {

    private final ProjectMemberQueryService projectMemberQueryService;

    // 마이페이지 전용 프로젝트 유저 조회
    @GetMapping
    public ApiResponse<ProjectUsersResDto> readProjectUsers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(projectMemberQueryService.readProjectUsers(projectId, userId));
    }
}
