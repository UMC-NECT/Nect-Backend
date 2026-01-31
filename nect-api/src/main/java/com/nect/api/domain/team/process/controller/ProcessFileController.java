package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.service.ProcessAttachmentService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/processes/{processId}/files")
public class ProcessFileController {
    private final ProcessAttachmentService processAttachmentService;

    @PostMapping
    public ApiResponse<ProcessFileAttachResDto> attach(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProcessFileAttachReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(processAttachmentService.attachFile(projectId, userId, processId, req));
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> detach(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        processAttachmentService.detachFile(projectId, userId, processId, fileId);
        return ApiResponse.ok(null);
    }
}
