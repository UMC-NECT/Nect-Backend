package com.nect.api.domain.mypage.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MyProjectStringListRequest(
        @NotNull
        List<String> contents
) {
}
