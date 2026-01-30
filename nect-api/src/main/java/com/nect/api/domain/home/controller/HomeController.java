package com.nect.api.domain.home.controller;

import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.service.HomeQueryService;
import com.nect.api.domain.home.service.HomeRecommendService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeQueryService queryService;
    private final HomeRecommendService recommendService;

    // 모집 중인 프로젝트 조회
    @GetMapping("/projects")
    public ApiResponse<HomeProjectResponse> recruitingProjects(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("count") int count){
        Long userId = (userDetails == null) ? null : userDetails.getUserId();
        HomeProjectResponse projects = queryService.getProjects(userId, count);
        return ApiResponse.ok(projects);
    }

    // 홈화면 프로젝트 추천
    @GetMapping("/recommendations/projects")
    public ApiResponse<HomeProjectResponse> recommendedProjects(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("count") int count){
        Long userId = (userDetails == null) ? null : userDetails.getUserId();
        HomeProjectResponse projects = recommendService.getProjects(userId, count);
        return ApiResponse.ok(projects);
    }

    // 홈화면 매칭 가능한 넥터
    @GetMapping("/members")
    public ApiResponse<HomeMembersResponse> matchableMembers(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("count") int count){
        Long userId = (userDetails == null) ? null : userDetails.getUserId();
        HomeMembersResponse members = queryService.getMembers(userId, count);
        return ApiResponse.ok(members);
    }

    // 홈화면 팀원 추천
    @GetMapping("/recommendations/members")
    public ApiResponse<HomeMembersResponse> recommendedMembers(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("count") int count){
        Long userId = (userDetails == null) ? null : userDetails.getUserId();
        HomeMembersResponse members = recommendService.getMembers(userId, count);
        return ApiResponse.ok(members);
    }

}
