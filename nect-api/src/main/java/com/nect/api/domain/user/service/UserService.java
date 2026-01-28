package com.nect.api.domain.user.service;

import com.nect.api.domain.user.dto.AgreeDto;
import com.nect.api.domain.user.dto.DuplicateCheckDto;
import com.nect.api.domain.user.dto.LoginDto;
import com.nect.api.domain.user.dto.SignUpDto;
import com.nect.api.domain.user.enums.CheckType;
import com.nect.api.domain.user.exception.*;
import com.nect.core.entity.user.enums.UserType;
import com.nect.core.entity.user.enums.Job;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.dto.TokenDataDto;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.user.TermUser;
import com.nect.core.entity.user.User;
import com.nect.core.repository.user.TermUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TermUserRepository termUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.auth.key}")
    private String authKey;

    @Transactional(readOnly = true)
    public LoginDto.LoginResponseDto refreshToken(String refreshToken) {
        TokenDataDto tokenData = jwtUtil.refreshToken(refreshToken);

        return LoginDto.LoginResponseDto.of(
                tokenData.getAccessToken(),
                tokenData.getRefreshToken(),
                tokenData.getAccessTokenExpiredAt(),
                tokenData.getRefreshTokenExpiredAt()
        );
    }

    @Transactional(readOnly = true)
    public LoginDto.LoginResponseDto testLoginByEmail(LoginDto.TestLoginRequestDto request) {
        validateTestLoginRequest(request);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        TokenDataDto tokenData = jwtUtil.createTokenData(user.getUserId());

        return LoginDto.LoginResponseDto.of(
                tokenData.getAccessToken(),
                tokenData.getRefreshToken(),
                tokenData.getAccessTokenExpiredAt(),
                tokenData.getRefreshTokenExpiredAt()
        );
    }

    public boolean checkDuplicate(DuplicateCheckDto.DuplicateCheckRequestDto request) {
        if (request.type() == null) {
            throw new InvalidCheckTypeException("검사 타입은 필수입니다");
        }

        if (request.value() == null || request.value().isEmpty()) {
            throw new InvalidCheckTypeException(request.type().getDescription() + "는 필수입니다");
        }

        return switch (request.type()) {
            case EMAIL -> userRepository.existsByEmail(request.value());
            case PHONE -> userRepository.existsByPhoneNumber(request.value());
        };
    }

    /**
     * 액세스 토큰으로 사용자 이메일 조회
     */
    @Transactional(readOnly = true)
    public LoginDto.EmailResponseDto getEmailByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return new LoginDto.EmailResponseDto(user.getEmail());
    }

    @Transactional
    public void signUp(SignUpDto.SignUpRequestDto request) {
        Job job = validateSignUpRequest(request);

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .name(request.name())
                .nickname(request.nickname())
                .phoneNumber(request.phoneNumber())
                .birthDate(request.birthDate())
                .userType(UserType.MEMBER)
                .job(job)
                .socialProvider(null)
                .socialId(null)
                .isAutoLoginEnabled(false)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginDto.LoginResponseDto login(LoginDto.LoginRequestDto request) {
        validateLoginRequest(request);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        TokenDataDto tokenData = jwtUtil.createTokenData(user.getUserId());

        return LoginDto.LoginResponseDto.of(
                tokenData.getAccessToken(),
                tokenData.getRefreshToken(),
                tokenData.getAccessTokenExpiredAt(),
                tokenData.getRefreshTokenExpiredAt()
        );
    }

    @Transactional
    public void logout(UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId();

        // 현재 요청에서 토큰 추출
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    jwtUtil.blacklistToken(token);
                } catch (Exception e) {
                    throw new InvalidCredentialsException("로그아웃 처리에 실패했습니다");
                }
            } else {
                log.warn("Authorization 헤더 없음 - userId: {}", userId);
            }
        }
    }

    @Transactional
    public void agree(Long userId, AgreeDto.AgreeRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!Boolean.TRUE.equals(request.termsAgreed())) {
            throw new ConsentRequiredException("서비스 이용약관 동의는 필수입니다");
        }

        if (!Boolean.TRUE.equals(request.privacyAgreed())) {
            throw new ConsentRequiredException("개인정보 수집 이용 동의는 필수입니다");
        }

        boolean marketingAgreed = Boolean.TRUE.equals(request.marketingAgreed());

        TermUser termUser = termUserRepository.findByUserUserId(userId)
                .orElse(null);

        if (termUser != null) {
            termUser = TermUser.builder()
                    .termId(termUser.getTermId())
                    .user(user)
                    .termsAgreed(request.termsAgreed())
                    .privacyAgreed(request.privacyAgreed())
                    .marketingAgreed(marketingAgreed)
                    .build();
        } else {
            termUser = TermUser.builder()
                    .user(user)
                    .termsAgreed(request.termsAgreed())
                    .privacyAgreed(request.privacyAgreed())
                    .marketingAgreed(marketingAgreed)
                    .build();
        }

        termUserRepository.save(termUser);

    }

    private void validateTestLoginRequest(LoginDto.TestLoginRequestDto request) {
        if (request == null) {
            throw new InvalidCredentialsException("요청 바디가 필수입니다");
        }

        validateEmailField(request.email());

        if (request.key() == null || request.key().isBlank()) {
            throw new InvalidCredentialsException("인증 키는 필수입니다");
        }
        if (!authKey.equals(request.key())) {
            log.warn("유효하지 않은 인증 키 시도");
            throw new InvalidCredentialsException("유효하지 않은 인증 키입니다");
        }
    }

    private Job validateSignUpRequest(SignUpDto.SignUpRequestDto request) {
        validateEmailField(request.email());

        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidPasswordException("비밀번호는 필수입니다");
        }
        if (request.password().length() < 8) {
            throw new InvalidPasswordException("비밀번호는 최소 8자 이상이어야 합니다");
        }

        if (request.passwordConfirm() == null || request.passwordConfirm().isBlank()) {
            throw new PasswordMismatchException("비밀번호 확인은 필수입니다");
        }
        if (!request.password().equals(request.passwordConfirm())) {
            throw new PasswordMismatchException("비밀번호 확인이 일치하지 않습니다");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidCredentialsException("이름은 필수입니다");
        }

        if (request.nickname() == null || request.nickname().isBlank()) {
            throw new InvalidCredentialsException("닉네임은 필수입니다");
        }

        if (request.job() == null || request.job().isBlank()) {
            throw new InvalidCredentialsException("직업은 필수입니다");
        }

        Job job;
        try {
            job = Job.valueOf(request.job().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid job value: {}", request.job());
            throw new InvalidJobTypeException("직업 타입은 EMPLOYEE, STUDENT, JOB_SEEKER, FREELANCER, BUSINESS_OWNER, OTHER입니다.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailDuplicateException();
        }

        return job;
    }

    private void validateLoginRequest(LoginDto.LoginRequestDto request) {
        if (request == null) {
            throw new InvalidCredentialsException("요청 바디가 필수입니다");
        }

        validateEmailField(request.email());

        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidCredentialsException("비밀번호는 필수입니다");
        }
    }

    private void validateEmailField(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException("이메일은 필수입니다");
        }
        if (!isValidEmail(email)) {
            throw new InvalidCredentialsException("올바른 이메일 형식이 아닙니다");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailRegex, email);
    }
}
