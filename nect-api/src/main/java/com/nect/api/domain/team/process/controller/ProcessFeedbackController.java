package com.nect.api.domain.team.process.controller;


import com.nect.api.domain.team.process.dto.req.ProcessFeedbackCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackCreateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackDeleteResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackUpdateResDto;
import com.nect.api.domain.team.process.service.ProcessFeedbackService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
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
            @RequestBody ProcessFeedbackCreateReqDto request
    ) {
        ProcessFeedbackCreateResDto res = processFeedbackService.createFeedback(projectId, processId, request);
        return ApiResponse.ok(res);
    }

    @PatchMapping("/{feedbackId}")
    public ApiResponse<ProcessFeedbackUpdateResDto> updateFeedback(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long feedbackId,
            @RequestBody ProcessFeedbackUpdateReqDto request
    ) {
        ProcessFeedbackUpdateResDto res =
                processFeedbackService.updateFeedback(projectId, processId, feedbackId, request);
        return ApiResponse.ok(res);
    }

    @DeleteMapping("/{feedbackId}")
    public ApiResponse<ProcessFeedbackDeleteResDto> deleteFeedback(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long feedbackId
    ) {
        ProcessFeedbackDeleteResDto res =
                processFeedbackService.deleteFeedback(projectId, processId, feedbackId);
        return ApiResponse.ok(res);
    }
}
