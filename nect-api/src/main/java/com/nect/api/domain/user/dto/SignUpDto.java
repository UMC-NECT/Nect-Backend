package com.nect.api.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nect.api.global.code.DateConstants;

import java.time.LocalDate;

public class SignUpDto {

    public record SignUpRequestDto(
            String email,
            String password,
            String passwordConfirm,
            String name,
            String nickname,
            String phoneNumber,
            @JsonFormat(pattern = DateConstants.DATE_PATTERN)
            LocalDate birthDate,
            String job
    ) {}

    public record SignUpResponseDto(
            Long userId,
            String email,
            String name,
            String nickname
    ) {
        public static SignUpResponseDto of(Long userId, String email, String name, String nickname) {
            return new SignUpResponseDto(
                    userId,
                    email,
                    name,
                    nickname
            );
        }
    }
}
