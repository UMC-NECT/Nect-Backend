package com.nect.api.matching.dto;

import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;

import java.time.LocalDateTime;

public class MatchingResDTO {

    public record matchingResDTO(
            Long id,
            Long requestUserId,
            Long targetUserId,
            Long projectId,
            MatchingStatus matchingStatus,
            MatchingRequestType requestType,
            LocalDateTime expiresAt
    ){}
}
