package com.nect.api.domain.user.dto;

import com.nect.api.domain.user.enums.CheckType;

public class DuplicateCheckDto {

    public record DuplicateCheckRequestDto(
            CheckType type,
            String value
    ) {}

    public record DuplicateCheckResponseDto(
            boolean available
    ) {}
}