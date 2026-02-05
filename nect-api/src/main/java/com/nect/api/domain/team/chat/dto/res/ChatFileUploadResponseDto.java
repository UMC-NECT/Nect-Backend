package com.nect.api.domain.team.chat.dto.res;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChatFileUploadResponseDto(
        Long fileId,
        String fileName,
        String fileUrl,
        Long fileSize,
        String fileType
) {}