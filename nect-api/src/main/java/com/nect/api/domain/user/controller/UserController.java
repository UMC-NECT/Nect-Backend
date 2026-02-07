package com.nect.api.domain.user.controller;

import com.nect.api.domain.user.dto.*;
import com.nect.api.domain.user.service.UserService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/refresh")
    public ApiResponse<LoginDto.TokenResponseDto> refreshToken(
            @Valid @RequestBody LoginDto.RefreshTokenRequestDto request
    ) {
        LoginDto.TokenResponseDto response = userService.refreshToken(request.refreshToken());
        return ApiResponse.ok(response);
    }

    @PostMapping("/test-login")
    public ApiResponse<LoginDto.TokenResponseDto> testLogin(
            @RequestBody(required = false) LoginDto.TestLoginRequestDto request
    ) {
        LoginDto.TokenResponseDto response = userService.testLoginByEmail(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.logout(userDetails);
        return ApiResponse.ok();
    }

    @PostMapping("/check")
    public ApiResponse<DuplicateCheckDto.DuplicateCheckResponseDto> checkDuplicate(
            @RequestBody DuplicateCheckDto.DuplicateCheckRequestDto request
    ) {
        boolean isDuplicate = userService.checkDuplicate(request);
        return ApiResponse.ok(new DuplicateCheckDto.DuplicateCheckResponseDto(!isDuplicate));
    }

    @PostMapping("/signup")
    public ApiResponse<LoginDto.TokenResponseDto> signUp(
            @RequestBody SignUpDto.SignUpRequestDto request
    ) {
        LoginDto.TokenResponseDto response = userService.signUp(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginDto.LoginResponseDto> login(
            @RequestBody(required = false) LoginDto.LoginRequestDto request
    ) {
        LoginDto.LoginResponseDto response = userService.login(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/agree")
    public ApiResponse<Void> agree(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody AgreeDto.AgreeRequestDto request
    ) {
        userService.agree(userDetails.getUserId(), request);
        return ApiResponse.ok();
    }

    @GetMapping("/email")
    public ApiResponse<LoginDto.EmailResponseDto> getEmail(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        LoginDto.EmailResponseDto response = userService.getEmailByUserId(userDetails.getUserId());
        return ApiResponse.ok(response);
    }

    @PostMapping("/profile/setup")
    public ApiResponse<Void> setupProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody(required = false) ProfileDto.ProfileSetupRequestDto request
    ) {
        userService.setupProfile(userDetails.getUserId(), request);
        return ApiResponse.ok();
    }

    @GetMapping("/info")
    public ApiResponse<ProfileDto.UserInfoResponseDto> getUserInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ProfileDto.UserInfoResponseDto response = userService.getUserInfo(userDetails.getUserId());
        return ApiResponse.ok(response);
    }

    @GetMapping("/profile/analysis")
    public ApiResponse<ProfileAnalysisDto> analyzeProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ProfileAnalysisDto response = userService.analyzeProfile(userDetails.getUserId());
        return ApiResponse.ok(response);
    }

    @GetMapping("/profile/analysis/projects")
    public ApiResponse<ProfileAnalysisDto.PaginatedResponse<ProfileAnalysisDto.RecommendedProjectInfo>> getRecommendedProjects(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable
    ) {
        ProfileAnalysisDto.PaginatedResponse<ProfileAnalysisDto.RecommendedProjectInfo> projects =
                userService.getRecommendedProjects(userDetails.getUserId(), pageable);
        return ApiResponse.ok(projects);
    }

    @GetMapping("/profile/analysis/team-members")
    public ApiResponse<ProfileAnalysisDto.PaginatedResponse<ProfileAnalysisDto.RecommendedTeamMemberInfo>> getRecommendedTeamMembers(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable
    ) {
        ProfileAnalysisDto.PaginatedResponse<ProfileAnalysisDto.RecommendedTeamMemberInfo> teamMembers =
                userService.getRecommendedTeamMembers(userDetails.getUserId(), pageable);
        return ApiResponse.ok(teamMembers);
    }

    @DeleteMapping("/profile/analysis")
    public ApiResponse<Void> deleteProfileAnalysis(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.deleteProfileAnalysis(userDetails.getUserId());
        return ApiResponse.ok();
    }
}
