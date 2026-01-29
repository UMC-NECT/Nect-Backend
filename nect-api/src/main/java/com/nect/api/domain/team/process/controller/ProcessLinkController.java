package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessLinkCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessLinkCreateResDto;
import com.nect.api.domain.team.process.service.ProcessAttachmentService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/processes/{processId}/links")
public class ProcessLinkController {

    private final ProcessAttachmentService processAttachmentService;

    @PostMapping
    public ApiResponse<ProcessLinkCreateResDto> create(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestBody ProcessLinkCreateReqDto req
    ) {
        return ApiResponse.ok(processAttachmentService.createLink(projectId, processId, req));
    }

    @DeleteMapping("/{linkId}")
    public ApiResponse<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long linkId
    ) {
        processAttachmentService.deleteLink(projectId, processId, linkId);
        return ApiResponse.ok(null);
    }
}