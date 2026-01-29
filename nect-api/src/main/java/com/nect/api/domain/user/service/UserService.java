package com.nect.api.domain.user.service;

import com.nect.api.domain.user.dto.AgreeDto;
import com.nect.api.domain.user.dto.DuplicateCheckDto;
import com.nect.api.domain.user.dto.LoginDto;
import com.nect.api.domain.user.dto.SignUpDto;
import com.nect.api.domain.user.dto.ProfileDto;
import com.nect.api.domain.user.enums.CheckType;
import com.nect.api.domain.user.exception.*;
import com.nect.api.domain.user.exception.InvalidInterestFieldException;
import com.nect.api.domain.user.exception.InvalidSkillCategoryException;
import com.nect.api.domain.user.exception.InvalidCollaborationScoreException;
import com.nect.core.entity.user.enums.UserType;
import com.nect.core.entity.user.enums.Job;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.entity.user.enums.Goal;
import com.nect.core.entity.user.enums.Skill;
import com.nect.core.entity.user.enums.SkillCategory;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.*;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.dto.TokenDataDto;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.user.TermUser;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserRole;
import com.nect.core.entity.user.UserSkill;
import com.nect.core.entity.user.UserInterest;
import com.nect.core.repository.user.TermUserRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserRoleRepository;
import com.nect.core.repository.user.UserSkillRepository;
import com.nect.core.repository.user.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TermUserRepository termUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserInterestRepository userInterestRepository;
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
            case NICKNAME -> userRepository.existsByNickname(request.value());
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
        validateSignUpRequest(request);

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .name(request.name())
                .nickname(null)
                .phoneNumber(request.phoneNumber())
                .birthDate(null)
                .userType(UserType.MEMBER)
                .job(null)
                .role(null)
                .firstGoal(null)
                .collaborationStylePlanning(null)
                .collaborationStyleLogic(null)
                .collaborationStyleLeadership(null)
                .socialProvider(null)
                .socialId(null)
                .isAutoLoginEnabled(false)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public LoginDto.LoginResponseDto login(LoginDto.LoginRequestDto request) {
        validateLoginRequest(request);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (Boolean.TRUE.equals(request.autoLoginEnabled())) {
            user.updateAutoLoginEnabled(true);
            userRepository.save(user);
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

    private void validateSignUpRequest(SignUpDto.SignUpRequestDto request) {
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

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailDuplicateException();
        }
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

    @Transactional
    public void setupProfile(Long userId, ProfileDto.ProfileSetupRequestDto request) {
        validateProfileSetupRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (userRepository.existsByNickname(request.nickname())) {
            throw new NicknameDuplicateException();
        }

        LocalDate birthDate = parseBirthDate(request.birthDate());
        Job job = parseJob(request.job());
        Role role = parseRole(request.role());
        Goal goal = parseGoal(request.firstGoal());

        User updatedUser = User.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType())
                .socialProvider(user.getSocialProvider())
                .socialId(user.getSocialId())
                .isAutoLoginEnabled(user.getIsAutoLoginEnabled())
                .nickname(request.nickname())
                .birthDate(birthDate)
                .job(job)
                .role(role)
                .firstGoal(goal)
                .collaborationStylePlanning(request.collaborationStyle() != null ? request.collaborationStyle().planning() : null)
                .collaborationStyleLogic(request.collaborationStyle() != null ? request.collaborationStyle().logic() : null)
                .collaborationStyleLeadership(request.collaborationStyle() != null ? request.collaborationStyle().leadership() : null)
                .build();
        userRepository.save(updatedUser);

        if (request.fields() != null) {
            for (ProfileDto.FieldDto fieldDto : request.fields()) {
                RoleField field = parseField(fieldDto.field());
                UserRole userRole = UserRole.builder()
                        .user(updatedUser)
                        .roleField(field)
                        .customField(field == RoleField.CUSTOM ? fieldDto.customField() : null)
                        .build();
                userRoleRepository.save(userRole);
            }
        }

        if (request.skills() != null) {
            for (ProfileDto.SkillDto skillDto : request.skills()) {
                Skill skill = parseSkill(skillDto.skill());
                UserSkill userSkill = UserSkill.builder()
                        .user(updatedUser)
                        .skillCategory(skillDto.skillCategory())
                        .skill(skill)
                        .customSkillName(skill == Skill.CUSTOM ? skillDto.customSkillName() : null)
                        .build();
                userSkillRepository.save(userSkill);
            }
        }

        if (request.interests() != null) {
            for (String interestStr : request.interests()) {
                InterestField interestField = parseInterestField(interestStr);
                UserInterest userInterest = UserInterest.builder()
                        .user(updatedUser)
                        .interestField(interestField)
                        .build();
                userInterestRepository.save(userInterest);
            }
        }
    }

    private void validateProfileSetupRequest(ProfileDto.ProfileSetupRequestDto request) {
        if (request == null) {
            throw new InvalidCredentialsException("요청 바디가 필수입니다");
        }

        if (request.nickname() == null || request.nickname().isBlank()) {
            throw new InvalidCredentialsException("닉네임은 필수입니다");
        }
        if (request.job() == null || request.job().isBlank()) {
            throw new InvalidCredentialsException("직업은 필수입니다");
        }
        if (request.role() == null || request.role().isBlank()) {
            throw new InvalidCredentialsException("역할은 필수입니다");
        }
        if (request.fields() == null || request.fields().isEmpty()) {
            throw new InvalidCredentialsException("직종은 최소 1개 이상 선택해야 합니다");
        }
        if (request.skills() == null || request.skills().isEmpty()) {
            throw new InvalidCredentialsException("상세 스킬은 최소 1개 이상 선택해야 합니다");
        }

        for (ProfileDto.SkillDto skillDto : request.skills()) {
            if (skillDto.skillCategory() == null) {
                throw new InvalidCredentialsException("스킬 카테고리는 필수입니다");
            }
            Skill skill = parseSkill(skillDto.skill());
            if (!skill.getCategory().equals(skillDto.skillCategory())) {
                throw new InvalidSkillCategoryException("선택한 스킬 카테고리(" + skillDto.skillCategory().getDescription() + ")과 스킬의 카테고리가 일치하지 않습니다: " + skillDto.skill());
            }
        }

        Role role = parseRole(request.role());
        for (ProfileDto.FieldDto fieldDto : request.fields()) {
            RoleField field = parseField(fieldDto.field());

            if (field != RoleField.CUSTOM && field.getRole() != null && !field.getRole().equals(role)) {
                throw new InvalidRoleFieldCombinationException("선택한 역할(" + role.getDescription() + ")과 맞지 않는 직종입니다: " + fieldDto.field());
            }
        }

        if (request.birthDate() != null && !request.birthDate().isBlank()) {
            if (request.birthDate().length() != 8) {
                throw new InvalidBirthDateFormatException("생년월일은 8글자로 입력해주세요 (예: 19990315)");
            }
        }

        if (request.collaborationStyle() != null) {
            if (request.collaborationStyle().planning() == null) {
                throw new InvalidCollaborationScoreException("협업 스타일 계획형은 필수입니다");
            }
            if (request.collaborationStyle().logic() == null) {
                throw new InvalidCollaborationScoreException("협업 스타일 논리형은 필수입니다");
            }
            if (request.collaborationStyle().leadership() == null) {
                throw new InvalidCollaborationScoreException("협업 스타일 리더형은 필수입니다");
            }
            validateCollaborationScore(request.collaborationStyle().planning());
            validateCollaborationScore(request.collaborationStyle().logic());
            validateCollaborationScore(request.collaborationStyle().leadership());
        } else {
            throw new InvalidCredentialsException("협업 스타일은 필수입니다");
        }
    }

    private Job parseJob(String jobStr) {
        try {
            return Job.valueOf(jobStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidJobTypeException("올바른 직업 타입이 아닙니다");
        }
    }

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("올바른 역할 타입이 아닙니다");
        }
    }

    private RoleField parseField(String fieldStr) {
        try {
            return RoleField.valueOf(fieldStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException("올바른 직종 타입이 아닙니다");
        }
    }

    private Goal parseGoal(String goalStr) {
        if (goalStr == null || goalStr.isBlank()) {
            return null;
        }
        try {
            return Goal.valueOf(goalStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidGoalException("올바른 목표 타입이 아닙니다");
        }
    }

    private void validateCollaborationScore(Integer score) {
        if (score != null && (score < 1 || score > 5)) {
            throw new InvalidCollaborationScoreException("협업 스타일 점수는 1-5 사이여야 합니다");
        }
    }

    private LocalDate parseBirthDate(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.isBlank()) {
            return null;
        }

        try {
            String year = birthDateStr.substring(0, 4);
            String month = birthDateStr.substring(4, 6);
            String day = birthDateStr.substring(6, 8);
            return LocalDate.of(Integer.parseInt(year),
                               Integer.parseInt(month),
                               Integer.parseInt(day));
        } catch (Exception e) {
            throw new InvalidBirthDateFormatException("올바른 생년월일 형식이 아닙니다");
        }
    }

    private Skill parseSkill(String skillStr) {
        try {
            return Skill.valueOf(skillStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidSkillCategoryException("올바른 스킬 타입이 아닙니다");
        }
    }

    private InterestField parseInterestField(String interestStr) {
        try {
            return InterestField.valueOf(interestStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInterestFieldException("올바른 관심분야 타입이 아닙니다");
        }
    }
}
