package com.nect.api.domain.mypage.controller;

import com.nect.api.domain.matching.service.RecruitmentService;
import com.nect.api.domain.mypage.dto.MyProjectStringListRequest;
import com.nect.api.domain.matching.dto.RecruitmentReqDto;
import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.*;
import com.nect.api.domain.mypage.service.MyPageProjectCommandService;
import com.nect.api.domain.mypage.service.MyPageProjectQueryService;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.ProfileSettingsRequestDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.ProfileSettingsResponseDto;
import com.nect.api.domain.mypage.service.MypageService;
import com.nect.api.domain.team.project.dto.ProjectUserFieldReqDto;
import com.nect.api.domain.team.project.dto.ProjectUserFieldResDto;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.domain.team.project.dto.ProjectUserTypeReqDto;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.team.enums.PlanFileType;
import com.nect.core.entity.user.enums.InterestField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;

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

    // 프로젝트 분야 조회
    @GetMapping("/projects/{projectId}/project-field")
    public ApiResponse<MyProjectsResponseDto.ProjectFieldResponse> getProjectField(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        return ApiResponse.ok(projectQueryService.getProjectFields(projectId));
    }

    // 프로젝트 분야 수정
    @PatchMapping("/projects/{projectId}/project-field")
    public ApiResponse<Void> editField(@PathVariable Long projectId, @RequestParam("field") InterestField interestField) {
        projectCommandService.changeProjectInterest(projectId, interestField);
        return ApiResponse.ok();
    }


    // 모집정보 추가

    // 프로젝트 목표 조회
    @GetMapping("/projects/{projectId}/purposes")
    public ApiResponse<MyProjectsResponseDto.StringListResponse> getPurposes(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(projectQueryService.getPurposes(projectId));
    }

    // 프로젝트 목표 작성
    @PatchMapping("/projects/{projectId}/purposes")
    public ApiResponse<Void> writePurposes(
            @PathVariable Long projectId,
            @Valid @RequestBody MyProjectStringListRequest request
    ) {
        projectCommandService.changePurpose(projectId, request.contents());
        return ApiResponse.ok();
    }

    // 주요기능 조회
    @GetMapping("/projects/{projectId}/functions")
    public ApiResponse<MyProjectsResponseDto.StringListResponse> getFunctions(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(projectQueryService.getFunctions(projectId));
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

    // 서비스 사용자 조회
    @GetMapping("/projects/{projectId}/service-users")
    public ApiResponse<MyProjectsResponseDto.StringListResponse> getServiceUsers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        return ApiResponse.ok(projectQueryService.getServiceUsers(projectId));
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

    // 프로젝트 세부 기획 파일 조회
    @GetMapping("/projects/{projectId}/plan-file")
    public ApiResponse<MyProjectsResponseDto.ProjectPlanFilesResponse> getPlanFiles(
            @PathVariable Long projectId
    ) {
        return ApiResponse.ok(projectQueryService.getPlanFiles(projectId));
    }

    // 프로젝트 세부 기획 파일 다운로드
    @GetMapping("/projects/{projectId}/plan-file/{planFileId}/download")
    public ApiResponse<MyProjectsResponseDto.ProjectPlanFileDownloadResponse> downloadPlanFile(
            @PathVariable Long projectId,
            @PathVariable Long planFileId
    ) {
        String url = projectQueryService.getPlanFileDownloadUrl(projectId, planFileId);
        return ApiResponse.ok(new MyProjectsResponseDto.ProjectPlanFileDownloadResponse(url));
    }

    // 프로젝트 세부 기획 파일 추가
    @PostMapping(value = "/projects/{projectId}/plan-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> uploadPlanFile(
            @PathVariable Long projectId,
            @RequestPart("name") String name,
            @RequestPart("planFileType") PlanFileType planFileType,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "link", required = false) String link
    ) {
        projectCommandService.addPlanFile(projectId, name, planFileType, file, link);
        return ApiResponse.ok();
    }

    // 프로젝트 세부 기획 파일 수정
    @PatchMapping(value = "/projects/{projectId}/plan-file/{planFileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> editPlanFile(
            @PathVariable Long projectId,
            @PathVariable Long planFileId,
            @RequestPart("name") String name,
            @RequestPart("planFileType") PlanFileType planFileType,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "link", required = false) String link
    ) {
        projectCommandService.editPlanFile(projectId, planFileId, name, planFileType, file, link);
        return ApiResponse.ok();
    }

    // 프로젝트 세부 기획 파일 삭제
    @DeleteMapping(value = "/projects/{projectId}/plan-file/{planFileId}")
    public ApiResponse<Void> removePlanFile(
            @PathVariable Long projectId,
            @PathVariable Long planFileId
    ){
        projectCommandService.removePlanFile(projectId, planFileId);
        return ApiResponse.ok();
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

}