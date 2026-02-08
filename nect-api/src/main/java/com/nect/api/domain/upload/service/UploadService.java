package com.nect.api.domain.upload.service;

import com.nect.api.domain.upload.dto.UploadDto;
import com.nect.api.global.infra.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final S3Service s3Service;

    public UploadDto.ImageUploadResponseDto uploadProfileImage(MultipartFile file) throws IOException {
        String fileName = s3Service.uploadFile(file);
        String fileUrl = s3Service.getPresignedGetUrl(fileName);

        return new UploadDto.ImageUploadResponseDto(fileName, fileUrl);
    }
}
