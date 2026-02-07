package com.nect.api.domain.upload.controller;

import com.nect.api.domain.upload.dto.UploadDto;
import com.nect.api.domain.upload.service.UploadService;
import com.nect.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/upload")
    public ApiResponse<UploadDto.ImageUploadResponseDto> uploadProfileImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UploadDto.ImageUploadResponseDto response = uploadService.uploadProfileImage(file);
        return ApiResponse.ok(response);
    }
}