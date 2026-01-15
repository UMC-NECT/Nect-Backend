package com.nect.api.matching.dto;

import com.nect.core.entity.matching.enums.MatchingRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MatchingReqDTO {

    public record matchingReqDTO(
            Long targetUserId,
            @NotNull
            Long projectId,
            @NotNull
            MatchingRequestType requestType
    ){}
}
