package com.nect.api.domain.team.file.controller;


import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class FileController {
    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResDto> upload(
            @PathVariable Long projectId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(fileService.upload(projectId, file));
    }
}
