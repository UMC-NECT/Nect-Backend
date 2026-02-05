package com.nect.api.domain.matching.dto;

import com.nect.api.domain.matching.enums.CounterParty;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.entity.user.enums.RoleField;
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
            RoleField field,
            String customField,
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
    public record MatchingCounts(
            int receivedCount,
            int sentCount
    ){}

    @Builder
    public record MatchingListRes(
            CounterParty counterParty,
            List<UserSummary> userMatchings,
            List<ProjectSummary> projectMatchings
    ){}

    @Builder
    public record UserSummary(
            String nickname,
            String bio,
            RoleField field,
            String profileUrl
    ){}

    @Builder
    public record ProjectSummary(
            String title,
            String description,
            long currentMembersNum,
            String imageUrl
    ){}
}
