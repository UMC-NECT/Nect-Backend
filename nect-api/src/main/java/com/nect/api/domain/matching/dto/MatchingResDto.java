package com.nect.api.domain.matching.dto;

import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class MatchingResDto {

    @Builder
    public record MatchingRes(
            Long id,
            Long requestUserId,
            Long targetUserId,
            Long projectId,
            Long fieldId,
            MatchingStatus matchingStatus,
            MatchingRequestType requestType,
            LocalDateTime expiresAt
    ){}

    @Builder
    public record MatchingAcceptResDto(
            MatchingResDto.MatchingRes matching,
            ProjectUserResDto projectUser
    ){}

    @Builder
    public record MatchingListRes(
            List<MatchingRes> matchings,
            int pendingRequestCount
    ){}
}
