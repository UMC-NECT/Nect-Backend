package com.nect.api.domain.home.controller;

import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.service.HomeQueryService;
import com.nect.api.domain.home.service.HomeRecommendService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<HomeProjectResponse> recruitingProjects(@RequestParam("count") int count){
        HomeProjectResponse projects = queryService.getProjects(1L, count);
        return ApiResponse.ok(projects);
    }

    // 홈화면 프로젝트 추천
    @GetMapping("/recommendations/projects")
    public ApiResponse<HomeProjectResponse> recommendedProjects(@RequestParam("count") int count){
        HomeProjectResponse projects = recommendService.getProjects(1L, count);
        return ApiResponse.ok(projects);
    }

    // 홈화면 매칭 가능한 넥터
    @GetMapping("/members")
    public ApiResponse<HomeMembersResponse> matchableMembers(@RequestParam("count") int count){
        HomeMembersResponse members = queryService.getMembers(1L, count);
        return ApiResponse.ok(members);
    }

    // 홈화면 팀원 추천
    @GetMapping("/recommendations/members")
    public ApiResponse<HomeMembersResponse> recommendedMembers(@RequestParam("count") int count){
        HomeMembersResponse members = recommendService.getMembers(1L, count);
        return ApiResponse.ok(members);
    }

}
