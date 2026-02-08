package com.nect.api.domain.mypage.controller;

import com.nect.api.domain.matching.dto.RecruitmentReqDto;
import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.api.domain.matching.service.RecruitmentService;
import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.ProfileSettingsRequestDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.ProfileSettingsResponseDto;
import com.nect.api.domain.mypage.service.MyPageProjectCommandService;
import com.nect.api.domain.mypage.service.MyPageProjectQueryService;
import com.nect.api.domain.mypage.service.MypageService;
import com.nect.api.domain.team.project.dto.ProjectUserFieldReqDto;
import com.nect.api.domain.team.project.dto.ProjectUserFieldResDto;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.domain.team.project.dto.ProjectUserTypeReqDto;
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
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;
    private final MyPageProjectQueryService projectQueryService;
    private final MyPageProjectCommandService projectCommandService;
    private final ProjectUserService projectUserService;
    private final RecruitmentService recruitmentService;

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

    /**
     * 프로젝트 멤버 필드 변경
     */
    @PatchMapping("/{projectUserId}/field")
    public ApiResponse<ProjectUserFieldResDto> updateProjectUserField(
            @PathVariable @Positive Long projectUserId,
            @RequestBody @Valid ProjectUserFieldReqDto reqDto
    ) {
        return ApiResponse.ok(projectUserService.changeProjectUserFieldInProject(projectUserId, reqDto));
    }

    /**
     * 프로젝트 멤버 내보내기 (상태 변경)
     */
    @PatchMapping("/{projectUserId}/kick")
    public ApiResponse<ProjectUserResDto> kickProjectUser(
            @PathVariable @Positive Long projectUserId,
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return ApiResponse.ok(projectUserService.kickProjectUser(user.getUserId(), projectUserId));
    }

    /**
     * 프로젝트 멤버 역할 변경 (LEADER | LEAD | MEMBER)
     */
    @PatchMapping("/{projectUserId}/type")
    public ApiResponse<ProjectUserResDto> updateProjectUserType(
            @PathVariable @Positive Long projectUserId,
            @RequestBody @Valid ProjectUserTypeReqDto req,
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return ApiResponse.ok(projectUserService.changeProjectUserTypeInProject(
                        user.getUserId(),
                        projectUserId,
                        req.memberType()
                )
        );
    }

    // TODO: 해주세요
    // 프로젝트 분야 수정

    // 모집정보 추가
    @PostMapping("/{projectId}/recruitments")
    public ApiResponse<RecruitmentResDto.EnrollRecruitmentResDto> createRecruitment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable @Positive Long projectId,
            @RequestBody @Valid RecruitmentReqDto.EnrollRecruitmentReqDto reqDto
    ){
        return ApiResponse.ok(recruitmentService.enrollRecruitment(user.getUserId(), projectId, reqDto));
    }

    // 모집정보 수정
    @PutMapping("/{projectId}/recruitments/{recruitmentId}")
    public ApiResponse<RecruitmentResDto.EnrollRecruitmentResDto> updateRecruitment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable @Positive Long projectId,
            @PathVariable @Positive Long recruitmentId,
            @RequestBody @Valid RecruitmentReqDto.EnrollRecruitmentReqDto reqDto
    ) {
        return ApiResponse.ok(recruitmentService.updateRecruitment(user.getUserId(), projectId, recruitmentId, reqDto));
    }

    // 프로젝트의 모집정보 전체 조회
    @GetMapping("/{projectId}/recruitments")
    public ApiResponse<List<RecruitmentResDto.EnrollRecruitmentResDto>> getRecruitmentsByProject(
            @PathVariable @Positive Long projectId
    ){
        return ApiResponse.ok(recruitmentService.getRecruitmentsByProject(projectId));
    }

    // 프로젝트 목표 추가

    // 프로젝트 목표 수정

    // 프로젝트 목표 삭제

    // 주요기능 추가

    // 주요기능 수정

    // 주요기능 삭제

    // 서비스 사용자 추가

    // 서비스 사용자 수정

    // 서비스 사용자 삭제

    // 프로젝트 세부 기획 파일 추가

    // 프로젝트 세부 기획 파일 삭제

}
