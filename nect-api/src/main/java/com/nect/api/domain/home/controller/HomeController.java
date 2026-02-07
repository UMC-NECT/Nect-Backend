package com.nect.api.domain.home.controller;

import com.nect.api.domain.home.dto.HomeHeaderResponse;
import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.facade.MainHomeFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final MainHomeFacade mainHomeFacade;

    // 모집 중인 프로젝트 조회, role, interest 필수 x
    @GetMapping("/projects")
    public ApiResponse<HomeProjectResponse> recruitingProjects(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("count") int count,
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "interest", required = false) InterestField interest
            ){
        Long userId = resolveUserId(userDetails);
        HomeProjectResponse projects = mainHomeFacade.getRecruitingProjects(userId, count, role, interest);
        return ApiResponse.ok(projects);
    }

    // 홈화면 프로젝트 추천
    @GetMapping("/recommendations/projects")
    public ApiResponse<HomeProjectResponse> recommendedProjects(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("count") int count){
        Long userId = resolveUserId(userDetails);
        HomeProjectResponse projects = mainHomeFacade.getRecommendedProjects(userId, count);
        return ApiResponse.ok(projects);
    }

    // 홈화면 매칭 가능한 넥터, , role, interest 필수x
    @GetMapping("/members")
    public ApiResponse<HomeMembersResponse> matchableMembers(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("count") int count,
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "interest", required = false) InterestField interest
    ){
        Long userId = resolveUserId(userDetails);
        HomeMembersResponse members = mainHomeFacade.getMatchableMembers(userId, count, role, interest);
        return ApiResponse.ok(members);
    }

    // 홈화면 팀원 추천
    @GetMapping("/recommendations/members")
    public ApiResponse<HomeMembersResponse> recommendedMembers(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("count") int count){
        Long userId = resolveUserId(userDetails);
        HomeMembersResponse members = mainHomeFacade.getRecommendedMembers(userId, count);
        return ApiResponse.ok(members);
    }

    // 홈화면 헤더 프로필
    @GetMapping("/profile")
    public ApiResponse<HomeHeaderResponse> headerProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = resolveUserId(userDetails);
        HomeHeaderResponse profileInfo = mainHomeFacade.getHeaderProfile(userId);
        return ApiResponse.ok(profileInfo);
    }

    private Long resolveUserId(UserDetailsImpl userDetails) {
        return (userDetails == null) ? null : userDetails.getUserId();
    }

}
