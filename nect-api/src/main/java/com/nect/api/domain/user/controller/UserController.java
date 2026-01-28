package com.nect.api.domain.user.controller;

import com.nect.api.domain.user.dto.AgreeDto;
import com.nect.api.domain.user.dto.DuplicateCheckDto;
import com.nect.api.domain.user.dto.LoginDto;
import com.nect.api.domain.user.dto.SignUpDto;
import com.nect.api.domain.user.service.UserService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/refresh")
    public ApiResponse<LoginDto.LoginResponseDto> refreshToken(
            @Valid @RequestBody LoginDto.RefreshTokenRequestDto request
    ) {
        LoginDto.LoginResponseDto response = userService.refreshToken(request.refreshToken());
        return ApiResponse.ok(response);
    }

    @PostMapping("/test-login")
    public ApiResponse<LoginDto.LoginResponseDto> testLogin(
            @RequestBody(required = false) LoginDto.TestLoginRequestDto request
    ) {
        LoginDto.LoginResponseDto response = userService.testLoginByEmail(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.logout(userDetails);
        return ApiResponse.ok();
    }

    @PostMapping("/check")
    public ApiResponse<DuplicateCheckDto.DuplicateCheckResponseDto> checkDuplicate(
            @RequestBody DuplicateCheckDto.DuplicateCheckRequestDto request
    ) {
        boolean isDuplicate = userService.checkDuplicate(request);
        return ApiResponse.ok(new DuplicateCheckDto.DuplicateCheckResponseDto(!isDuplicate));
    }

    @PostMapping("/signup")
    public ApiResponse<Void> signUp(
            @RequestBody SignUpDto.SignUpRequestDto request
    ) {
        userService.signUp(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginDto.LoginResponseDto> login(
            @RequestBody(required = false) LoginDto.LoginRequestDto request
    ) {
        LoginDto.LoginResponseDto response = userService.login(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/agree")
    public ApiResponse<Void> agree(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody AgreeDto.AgreeRequestDto request
    ) {
        userService.agree(userDetails.getUserId(), request);
        return ApiResponse.ok();
    }

    @GetMapping("/email")
    public ApiResponse<LoginDto.EmailResponseDto> getEmail(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        LoginDto.EmailResponseDto response = userService.getEmailByUserId(userDetails.getUserId());
        return ApiResponse.ok(response);
    }
}
