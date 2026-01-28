package com.nect.api.domain.team.file.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.enums.FileExt;

public record FileUploadResDto(
    @JsonProperty("file_id")
    Long fileId,

    @JsonProperty("file_name")
    String fileName,

    @JsonProperty("file_url")
    String fileUrl,

    @JsonProperty("file_type")
    FileExt fileType,

    @JsonProperty("file_size")
    Long fileSize
) {}
