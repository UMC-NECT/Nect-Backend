package com.nect.api.domain.matching.dto;

import com.nect.core.entity.matching.enums.MatchingRejectReason;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.validation.constraints.NotNull;

public class MatchingReqDto {

    public record matchingReqDto(
            @NotNull RoleField field,
            String customField
    ){}

    public record matchingRejectReqDto(
            @NotNull MatchingRejectReason rejectReason
    ){}
}
