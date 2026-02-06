package com.nect.api.domain.mypage.controller;

import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.*;
import com.nect.api.domain.mypage.service.MypageService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

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
     * 프로필 분석 불러오기
     */
    @GetMapping("/profile-analysis")
    public ApiResponse<ProfileSettingsDto.ProfileAnalysisResponseDto> getProfileAnalysis(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(mypageService.getProfileAnalysis(userDetails.getUserId()));
    }
}