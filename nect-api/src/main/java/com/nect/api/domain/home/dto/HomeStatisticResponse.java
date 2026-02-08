package com.nect.api.domain.home.dto;

public record HomeStatisticResponse(
    Integer totalProjectCount,
    Integer matchingSuccessRate,
    Integer reParticipateRate,
    Integer totalUserCount
) {
}
