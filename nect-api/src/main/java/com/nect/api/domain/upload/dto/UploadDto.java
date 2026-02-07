package com.nect.api.domain.upload.dto;

public class UploadDto {

    public record ImageUploadResponseDto(
            String fileName,
            String fileUrl
    ) {}
}