package com.nect.api.domain.matching.dto;

import jakarta.validation.constraints.NotNull;

public class MatchingReqDto {

    public record matchingReqDto(
            @NotNull Long fieldId
    ){}
}
