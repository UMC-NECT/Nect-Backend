package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.service.ProcessAttachmentService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/processes/{processId}/files")
public class ProcessFileController {
    private final ProcessAttachmentService processAttachmentService;

    @PostMapping
    public ApiResponse<ProcessFileAttachResDto> attach(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestBody ProcessFileAttachReqDto req
    ) {
        return ApiResponse.ok(processAttachmentService.attachFile(projectId, processId, req));
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> detach(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long fileId
    ) {
        processAttachmentService.detachFile(projectId, processId, fileId);
        return ApiResponse.ok(null);
    }
}
