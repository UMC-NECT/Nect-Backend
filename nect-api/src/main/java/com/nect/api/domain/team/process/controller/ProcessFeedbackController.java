package com.nect.api.domain.team.process.controller;


import com.nect.api.domain.team.process.dto.req.ProcessFeedbackCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackCreateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackDeleteResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackUpdateResDto;
import com.nect.api.domain.team.process.service.ProcessFeedbackService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/processes/{processId}/feedbacks")
public class ProcessFeedbackController {

    private final ProcessFeedbackService processFeedbackService;


    @PostMapping
    public ApiResponse<ProcessFeedbackCreateResDto> createFeedback(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProcessFeedbackCreateReqDto request
    ) {
        Long userId = userDetails.getUserId();
        ProcessFeedbackCreateResDto res = processFeedbackService.createFeedback(projectId, userId, processId, request);
        return ApiResponse.ok(res);
    }

    @PatchMapping("/{feedbackId}")
    public ApiResponse<ProcessFeedbackUpdateResDto> updateFeedback(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProcessFeedbackUpdateReqDto request
    ) {
        Long userId = userDetails.getUserId();
        ProcessFeedbackUpdateResDto res = processFeedbackService.updateFeedback(projectId, userId, processId, feedbackId, request);
        return ApiResponse.ok(res);
    }

    @DeleteMapping("/{feedbackId}")
    public ApiResponse<ProcessFeedbackDeleteResDto> deleteFeedback(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        ProcessFeedbackDeleteResDto res = processFeedbackService.deleteFeedback(projectId, userId, processId, feedbackId);
        return ApiResponse.ok(res);
    }
}
