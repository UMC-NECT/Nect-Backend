package com.nect.api.domain.analysis.controller;

import com.nect.api.domain.analysis.dto.req.ProjectCreateRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisPageResponseDto;
import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.dto.res.ProjectCreateResponseDto;
import com.nect.api.domain.analysis.service.IdeaAnalysisService;
import com.nect.api.domain.team.project.service.ProjectService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class IdeaAnalysisController {

    private final IdeaAnalysisService ideaAnalysisService;
    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<IdeaAnalysisPageResponseDto> getAnalysisPage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page) {

        Long userId = userDetails.getUserId();
        IdeaAnalysisPageResponseDto response = ideaAnalysisService.getAnalysisPage(userId, page);

        return ApiResponse.ok(response);
    }


    @PostMapping
    public ApiResponse<IdeaAnalysisResponseDto> analyzeIdea(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody IdeaAnalysisRequestDto requestDto) {

        Long userId = userDetails.getUserId();
        IdeaAnalysisResponseDto response = ideaAnalysisService.analyzeProjectIdea(userId, requestDto);

        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{analysisId}")
    public ApiResponse<Void> deleteAnalysis(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long analysisId) {

        Long userId = userDetails.getUserId();
        ideaAnalysisService.deleteAnalysis(userId, analysisId);

        return ApiResponse.ok();
    }
    
    @PostMapping("/{analysisId}/project")
    public ApiResponse<ProjectCreateResponseDto> createProjectFromAnalysis(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long analysisId) {

        Long userId = userDetails.getUserId();
        ProjectCreateRequestDto request = new ProjectCreateRequestDto(analysisId);
        ProjectCreateResponseDto response = projectService.createProjectFromAnalysis(userId, request);

        return ApiResponse.ok(response);
    }
}


