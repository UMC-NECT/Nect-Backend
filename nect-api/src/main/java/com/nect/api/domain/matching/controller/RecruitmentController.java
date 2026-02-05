package com.nect.api.domain.matching.controller;

import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.api.domain.matching.service.RecruitmentService;
import com.nect.api.domain.team.project.dto.RecruitingProjectResDto;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recruitments")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    // 해당 프로젝트가 모집 중인 분야 조회
    @GetMapping("/{projectId}")
    public ApiResponse<List<RecruitmentResDto.RecruitingFieldDto>> getRecruitingFields(
            @PathVariable @Positive Long projectId
    ) {
        return ApiResponse.ok(recruitmentService.findRecruitingFields(projectId));
    }

    // 내가 리더로 있고, 현재 모집중인 프로젝트 조회
    @GetMapping("/leader")
    public ApiResponse<List<RecruitingProjectResDto>> getMyRecruitingProjectAsLeader(
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        return ApiResponse.ok(recruitmentService.getMyRecruitingProjectAsLeader(user.getUserId()));
    }
}
