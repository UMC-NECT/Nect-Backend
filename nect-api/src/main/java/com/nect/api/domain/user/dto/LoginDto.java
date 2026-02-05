package com.nect.api.domain.user.dto;

public class LoginDto {

    public record LoginRequestDto(
            String email,
            String password,
            Boolean autoLoginEnabled
    ) {}

    public record TestLoginRequestDto(
            String email,
            String key
    ) {}

    public record LoginResponseDto(
            String grantType,
            String accessToken,
            String refreshToken,
            Long accessTokenExpiredAt,
            Long refreshTokenExpiredAt,
            Boolean isOnboardingCompleted
    ) {
        public static LoginResponseDto of(
                String accessToken,
                String refreshToken,
                Long accessTokenExpiredAt,
                Long refreshTokenExpiredAt,
                Boolean isOnboardingCompleted
        ) {
            return new LoginResponseDto(
                    "Bearer",
                    accessToken,
                    refreshToken,
                    accessTokenExpiredAt,
                    refreshTokenExpiredAt,
                    isOnboardingCompleted
            );
        }
    }

    public record CheckEmailResponseDto(
            boolean available
    ) {}

    public record CheckPhoneResponseDto(
            boolean available
    ) {}

    public record EmailResponseDto(
            String email
    ) {}

    public record RefreshTokenRequestDto(
            String refreshToken
    ) {}

    public record LogoutResponseDto(
            String message
    ) {}

    public record TokenResponseDto(
            String grantType,
            String accessToken,
            String refreshToken,
            Long accessTokenExpiredAt,
            Long refreshTokenExpiredAt
    ) {
        public static TokenResponseDto of(
                String accessToken,
                String refreshToken,
                Long accessTokenExpiredAt,
                Long refreshTokenExpiredAt
        ) {
            return new TokenResponseDto(
                    "Bearer",
                    accessToken,
                    refreshToken,
                    accessTokenExpiredAt,
                    refreshTokenExpiredAt
            );
        }
    }
}
