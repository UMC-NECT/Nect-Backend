package com.nect.api.domain.matching.converter;

import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRequestType;

public class MatchingConverter {

    public static MatchingResDto.MatchingRes toMatchingResDto(Matching matching) {
        return MatchingResDto.MatchingRes.builder()
                .id(matching.getId())
                .requestUserId(matching.getRequestUserId())
                .targetUserId(matching.getTargetUserId())
                .projectId(matching.getProjectId())
                .fieldId(matching.getFieldId())
                .matchingStatus(matching.getMatchingStatus())
                .requestType(matching.getRequestType())
                .expiresAt(matching.getExpiresAt())
                .build();
    }

    public static Matching toMatching(
            Long requestUserId,
            Long targetUserId,
            Long projectId,
            Long fieldId,
            MatchingRequestType requestType
    ){
        return Matching.builder()
                .requestUserId(requestUserId)
                .targetUserId(targetUserId)
                .projectId(projectId)
                .fieldId(fieldId)
                .requestType(requestType)
                .build();
    }
}
