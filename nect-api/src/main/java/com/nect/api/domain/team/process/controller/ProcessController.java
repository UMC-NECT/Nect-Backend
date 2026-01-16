package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessCreateResDto;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/processes")
@RequiredArgsConstructor
public class ProcessController {

    // TODO : private final ProcessService processService

    // 새 프로세스 생성
    @PostMapping
    public ApiResponse<ProcessCreateResDto> createProcess(
            @PathVariable Long projectId,
            @RequestBody ProcessCreateReqDto request
            ) {
        // TODO :  Long processId = processService.createProcess(projectId, request);

        Long processId = 1L; // 임시
        return ApiResponse.ok(new ProcessCreateResDto(processId));
    }

}
