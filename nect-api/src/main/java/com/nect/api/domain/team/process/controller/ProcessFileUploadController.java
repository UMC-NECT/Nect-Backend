package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.facade.ProcessAttachmentFacade;
import com.nect.api.domain.team.process.dto.res.ProcessFileUploadAndAttachResDto;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/processes/{processId}/files")
public class ProcessFileUploadController {

    private final ProcessAttachmentFacade processAttachmentFacade;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProcessFileUploadAndAttachResDto> uploadAndAttach(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(processAttachmentFacade.uploadAndAttachFile(projectId, processId, file));
    }
}
