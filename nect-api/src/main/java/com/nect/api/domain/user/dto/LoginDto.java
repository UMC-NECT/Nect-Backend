package com.nect.api.domain.user.dto;

public class LoginDto {

    public record LoginRequestDto(
            String email,
            String password
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
            Long refreshTokenExpiredAt
    ) {
        public static LoginResponseDto of(
                String accessToken,
                String refreshToken,
                Long accessTokenExpiredAt,
                Long refreshTokenExpiredAt
        ) {
            return new LoginResponseDto(
                    "Bearer",
                    accessToken,
                    refreshToken,
                    accessTokenExpiredAt,
                    refreshTokenExpiredAt
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
}
