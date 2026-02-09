package com.nect.api.domain.mypage.controller;

import com.nect.api.domain.mypage.dto.*;
import com.nect.api.domain.mypage.service.UserTeamRoleQueryService;
import com.nect.api.domain.mypage.service.UserTeamRoleService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/projects/{projectId}/team-roles")
public class UserTeamRoleController {

    private final UserTeamRoleService userTeamRoleService;
    private final UserTeamRoleQueryService userTeamRoleQueryService;


    // 마이페이지 팀 파트 생성(추가)
    @PostMapping
    public ApiResponse<UserTeamRoleCreateResDto> create(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody UserTeamRoleCreateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(userTeamRoleService.create(projectId, userId, req));
    }

    // 마이페이지 팀 파트 수정 (CUSTOM만 가능)
    @PatchMapping("/{userTeamRoleId}")
    public ApiResponse<UserTeamRoleUpdateResDto> update(
            @PathVariable("projectId") Long projectId,
            @PathVariable("userTeamRoleId") Long userTeamRoleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody UserTeamRoleUpdateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(userTeamRoleService.update(projectId, userId, userTeamRoleId, req));
    }

    // 마이페이지 파트 목록 조회
    @GetMapping
    public ApiResponse<UserTeamRolesResDto> readMyPageParts(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(userTeamRoleQueryService.readMyPageParts(projectId, userId));
    }
}
