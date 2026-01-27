package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessBasicUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessBasicUpdateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessCreateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessDetailResDto;
import com.nect.api.domain.team.process.service.ProcessService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/processes")
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


}
