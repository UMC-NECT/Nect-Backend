package com.nect.api.matching.dto;

import com.nect.core.entity.matching.enums.MatchingRequestType;
import jakarta.validation.constraints.NotNull;

public class MatchingReqDto {

    public record matchingReqDto(
            Long targetUserId,
            @NotNull
            Long projectId,
            @NotNull
            MatchingRequestType requestType
    ){}
}
