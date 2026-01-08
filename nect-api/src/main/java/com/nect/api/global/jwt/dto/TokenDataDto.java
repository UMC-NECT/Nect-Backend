package com.nect.api.global.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDataDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiredAt;
    private Long refreshTokenExpiredAt;
}