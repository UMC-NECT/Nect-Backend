package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import com.nect.core.entity.team.enums.FileExt;

public record AttachmentSummaryDto(
        @JsonProperty("total_count")
        long totalCount,

        @JsonProperty("file_count")
        long fileCount,

        @JsonProperty("link_count")
        long linkCount,

        @JsonProperty("file_extensions")
        List<FileExt> fileExtensions
) {
    public static AttachmentSummaryDto empty() {
        return new AttachmentSummaryDto(0, 0, 0, List.of());
    }
}

