package com.nect.api.domain.mypage.controller;

import com.nect.api.domain.mypage.dto.MyProjectStringListRequest;
import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.*;
import com.nect.api.domain.mypage.service.MyPageProjectCommandService;
import com.nect.api.domain.mypage.service.MyPageProjectQueryService;
import com.nect.api.domain.mypage.service.MypageService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;
    private final MyPageProjectQueryService projectQueryService;
    private final MyPageProjectCommandService projectCommandService;

    /**
     * 프로필 조회
     */
    @GetMapping("/profile")
    public ApiResponse<ProfileSettingsResponseDto> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(mypageService.getProfile(userDetails.getUserId()));
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/profile/save")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProfileSettingsRequestDto request
    ) {
        mypageService.updateProfile(userDetails.getUserId(), request);
        return ApiResponse.ok();
    }

    /**
     * 프로젝트 조회
     */
    @GetMapping("/projects")
    public ApiResponse<MyProjectsResponseDto> getMyProjects(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MyProjectsResponseDto response = projectQueryService.getMyProjects(userDetails.getUserId());

        return ApiResponse.ok(response);
    }


    /**
     * 프로필 분석 불러오기
     */
    @GetMapping("/profile-analysis")
    public ApiResponse<ProfileSettingsDto.ProfileAnalysisResponseDto> getProfileAnalysis(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(mypageService.getProfileAnalysis(userDetails.getUserId()));
    }

    // TODO: 해주세요
    // 프로젝트 분야 수정

    // 모집정보 추가

    // 프로젝트 목표 작성
    @PatchMapping("/projects/{projectId}/purposes")
    public ApiResponse<Void> writePurposes(
            @PathVariable Long projectId,
            @Valid @RequestBody MyProjectStringListRequest request
    ) {
        projectCommandService.changePurpose(projectId, request.contents());
        return ApiResponse.ok();
    }


    // 주요기능 작성
    @PatchMapping("/projects/{projectId}/functions")
    public ApiResponse<Void> writeMainFunctions(
            @PathVariable Long projectId,
            @Valid @RequestBody MyProjectStringListRequest request
    ) {
        projectCommandService.changeMainFunctions(projectId, request.contents());
        return ApiResponse.ok();
    }


    // 서비스 사용자 작성
    @PatchMapping("/projects/{projectId}/service-users")
    public ApiResponse<Void> writeServiceUsers(
            @PathVariable Long projectId,
            @Valid @RequestBody MyProjectStringListRequest request
    ) {
        projectCommandService.changeServiceUsers(projectId, request.contents());
        return ApiResponse.ok();
    }


    // 프로젝트 세부 기획 파일 추가

    // 프로젝트 세부 기획 파일 삭제

}
