package com.nect.api.domain.user.dto;

public class AgreeDto {

    public record AgreeRequestDto(
            Boolean termsAgreed,
            Boolean privacyAgreed,
            Boolean marketingAgreed
    ) {}

    public record AgreeResponseDto(
            Long userId
    ) {
        public static AgreeResponseDto of(Long userId) {
            return new AgreeResponseDto(userId);
        }
    }
}