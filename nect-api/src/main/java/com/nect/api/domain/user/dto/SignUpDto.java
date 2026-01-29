package com.nect.api.domain.user.dto;

public class SignUpDto {

    public record SignUpRequestDto(
            String email,
            String password,
            String passwordConfirm,
            String name,
            String phoneNumber
    ) {}
}
