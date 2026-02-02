package com.nect.api.domain.analysis.dto.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProjectCreateRequestDto(
        @NotNull(message = "분석서 ID는 필수입니다.")
        @Positive(message = "유효한 분석서 ID를 입력해주세요.")
        Long analysisId
) {}