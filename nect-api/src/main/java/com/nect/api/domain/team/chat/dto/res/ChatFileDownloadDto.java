package com.nect.api.domain.team.chat.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Getter
@Builder
public class ChatFileDownloadDto {
    private String fileName;
    private String fileType;
    private Long fileSize;
    private InputStream inputStream;
}