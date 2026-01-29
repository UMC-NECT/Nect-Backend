package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessBasicUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessOrderUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessStatusUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.service.ProcessService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
            @RequestBody ProcessCreateReqDto request
    ) {
        Long processId = processService.createProcess(projectId, request);

        return ApiResponse.ok(new ProcessCreateResDto(processId));
    }

    // 프로세스 상세 조회
    @GetMapping("/{processId}")
    public ApiResponse<ProcessDetailResDto> getProcessDetail(
            @PathVariable Long projectId,
            @PathVariable Long processId
    ) {
        ProcessDetailResDto res = processService.getProcessDetail(projectId, processId);
        return ApiResponse.ok(res);
    }

    // 프로세스 기본 정보 수정
    @PatchMapping("/{processId}")
    public ApiResponse<ProcessBasicUpdateResDto> updateProcessBasic(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestBody ProcessBasicUpdateReqDto request
    ) {
        ProcessBasicUpdateResDto res = processService.updateProcessBasic(projectId, processId, request);
        return ApiResponse.ok(res);
    }

    // 프로세스 삭제
    @DeleteMapping("/{processId}")
    public ApiResponse<Void> deleteProcess(
            @PathVariable Long projectId,
            @PathVariable Long processId
    ){
        processService.deleteProcess(projectId, processId);
        return ApiResponse.ok(null);
    }

    // 주차별 프로세스 조회
    @GetMapping("/week")
    public ApiResponse<ProcessWeekResDto> getWeekProcesses(
            @PathVariable Long projectId,
            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate
    ) {
        return ApiResponse.ok(processService.getWeekProcesses(projectId, startDate));
    }

    // 파트별 작업 현황 조회
    @GetMapping("/part")
    public ApiResponse<ProcessPartResDto> getPartProcesses(
            @PathVariable Long projectId,
            @RequestParam(name = "field_id", required = false) Long fieldId // Team 탭이면 null
    ) {
        return ApiResponse.ok(processService.getPartProcesses(projectId, fieldId));
    }


    // 프로세스 위치 상태 변경
    @PatchMapping("/{processId}/order")
    public ApiResponse<ProcessOrderUpdateResDto> updateProcessOrder(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestBody ProcessOrderUpdateReqDto request
    ) {
        return ApiResponse.ok(processService.updateProcessOrder(projectId, processId, request));
    }

    // 프로세스 작업 상태 변경
    @PatchMapping("/{processId}/status")
    public ApiResponse<ProcessStatusUpdateResDto> updateProcessStatus(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestBody ProcessStatusUpdateReqDto request
    ) {
        return ApiResponse.ok(processService.updateProcessStatus(projectId, processId, request));
    }

}
