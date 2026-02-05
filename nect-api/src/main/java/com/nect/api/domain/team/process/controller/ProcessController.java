package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessBasicUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessOrderUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessStatusUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.service.ProcessService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    // 새 프로세스 생성
    @PostMapping
    public ApiResponse<ProcessCreateResDto> createProcess(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ProcessCreateReqDto request
    ) {
        Long userId = userDetails.getUserId();
        ProcessCreateResDto res = processService.createProcess(projectId, userId, request);
        return ApiResponse.ok(res);
    }

    // 프로세스 상세 조회
    @GetMapping("/{processId}")
    public ApiResponse<ProcessDetailResDto> getProcessDetail(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestParam(value = "laneKey", required = false) String laneKey,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processService.getProcessDetail(projectId, userId, processId, laneKey));
    }

    // 프로세스 기본 정보 수정
    @PatchMapping("/{processId}")
    public ApiResponse<ProcessBasicUpdateResDto> updateProcessBasic(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProcessBasicUpdateReqDto request
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processService.updateProcessBasic(projectId, userId, processId, request));
    }

    // 프로세스 삭제
    @DeleteMapping("/{processId}")
    public ApiResponse<Void> deleteProcess(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long userId = userDetails.getUserId();
        processService.deleteProcess(projectId, userId, processId);
        return ApiResponse.ok(null);
    }

    // 주차별 프로세스 조회
    @GetMapping("/week")
    public ApiResponse<ProcessWeekResDto> getWeekProcesses(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processService.getWeekProcesses(projectId, userId, startDate));
    }

    // 파트별 작업 현황 조회
    @GetMapping("/part")
    public ApiResponse<ProcessPartResDto> getPartProcesses(
            @PathVariable Long projectId,
            @RequestParam(name = "lane_key", required = false) String laneKey,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processService.getPartProcesses(projectId, userId, laneKey));
    }

    // 프로세스 위치 상태 변경
    @PatchMapping("/{processId}/order")
    public ApiResponse<ProcessOrderUpdateResDto> updateProcessOrder(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProcessOrderUpdateReqDto request
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processService.updateProcessOrder(projectId, userId, processId, request));
    }

    // 프로세스 작업 상태 변경
    @PatchMapping("/{processId}/status")
    public ApiResponse<ProcessStatusUpdateResDto> updateProcessStatus(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProcessStatusUpdateReqDto request
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processService.updateProcessStatus(projectId, userId, processId, request));
    }

    // 프로세스 작업 진행률 조회
    @GetMapping("/parts/progress-summary")
    public ApiResponse<ProcessProgressSummaryResDto> getProcessProgressSummary(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processService.getPartProgressSummary(projectId, userId));
    }
}
