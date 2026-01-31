package com.nect.api.domain.analysis.controller;

import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.service.IdeaAnalysisService;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class IdeaAnalysisController {

    private final IdeaAnalysisService ideaAnalysisService;

    /*
     * 프로젝트 아이디어 분석 요청
     */
    @PostMapping
    public ResponseEntity<IdeaAnalysisResponseDto> analyzeIdea(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody IdeaAnalysisRequestDto requestDto) {

        Long userId = userDetails.getUserId();
        IdeaAnalysisResponseDto response = ideaAnalysisService.analyzeProjectIdea(userId, requestDto);

        return ResponseEntity.ok(response);
    }
}