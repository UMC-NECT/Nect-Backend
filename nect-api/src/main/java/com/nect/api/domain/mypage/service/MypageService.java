
package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto.*;
import com.nect.api.domain.mypage.exception.InvalidUserStatusException;
import com.nect.api.domain.mypage.exception.UserNotFoundException;
import com.nect.core.entity.user.*;
import com.nect.core.entity.user.enums.*;
import com.nect.core.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;
    private final UserCareerRepository userCareerRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final UserProjectHistoryRepository userProjectHistoryRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserProfileAnalysisRepository userProfileAnalysisRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ProfileSettingsResponseDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        List<UserCareer> careers = userCareerRepository.findByUserUserId(userId);
        List<CareerDto> careerDto = careers.stream()
                .map(career -> {
                    List<UserAchievement> achievements = userAchievementRepository
                            .findByUserCareerUserCareerId(career.getUserCareerId());
                    List<AchievementDto> achievementDto = achievements.stream()
                            .map(achievement -> new AchievementDto(
                                    achievement.getUserAchievementId(),
                                    achievement.getTitle(),
                                    achievement.getContent()
                            ))
                            .collect(Collectors.toList());

                    return new CareerDto(
                            career.getUserCareerId(),
                            career.getProjectName(),
                            career.getIndustryField(),
                            career.getStartDate(),
                            career.getEndDate(),
                            career.getIsOngoing(),
                            career.getRole(),
                            achievementDto
                    );
                })
                .collect(Collectors.toList());

        List<UserPortfolio> portfolios = userPortfolioRepository.findByUserUserId(userId);
        List<PortfolioDto> portfolioDto = portfolios.stream()
                .map(portfolio -> new PortfolioDto(
                        portfolio.getUserPortfolioId(),
                        portfolio.getTitle(),
                        portfolio.getLink(),
                        portfolio.getFileUrl()
                ))
                .collect(Collectors.toList());

        List<UserProjectHistory> projectHistories = userProjectHistoryRepository.findByUserUserId(userId);
        List<ProjectHistoryDto> projectHistoryDto = projectHistories.stream()
                .map(projectHistory -> new ProjectHistoryDto(
                        projectHistory.getUserProjectHistoryId(),
                        projectHistory.getProjectName(),
                        projectHistory.getProjectImage(),
                        projectHistory.getProjectDescription(),
                        projectHistory.getStartYearMonth(),
                        projectHistory.getEndYearMonth()
                ))
                .collect(Collectors.toList());

        List<UserSkill> skills = userSkillRepository.findByUserUserId(userId);
        List<SkillDto> skillDto = skills.stream()
                .collect(Collectors.groupingBy(userSkill -> userSkill.getSkill().getCategory()))
                .entrySet().stream()
                .map(entry -> {
                    SkillCategory category = entry.getKey();
                    List<SkillItemDto> items = entry.getValue().stream()
                            .map(userSkill -> new SkillItemDto(
                                    userSkill.getSkill().name(),
                                    userSkill.getSkill().getDisplayName(),
                                    true
                            ))
                            .collect(Collectors.toList());
                    return new SkillDto(
                            category.name(),
                            category.getDescription(),
                            items
                    );
                })
                .collect(Collectors.toList());

        // 프로필 분석 정보 조회
        String profileType = null;
        List<String> tags = null;

        var profileAnalysis = userProfileAnalysisRepository.findByUser(user);
        if (profileAnalysis.isPresent()) {
            profileType = profileAnalysis.get().getProfileType();
            if (profileAnalysis.get().getTags() != null) {
                try {
                    tags = objectMapper.readValue(profileAnalysis.get().getTags(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                } catch (Exception e) {
                    // JSON 파싱 실패 시 null
                    tags = null;
                }
            }
        }

        return new ProfileSettingsResponseDto(
                user.getUserId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getProfileImageUrl(),
                user.getBio(),
                user.getCoreCompetencies(),
                user.getUserStatus() != null ? user.getUserStatus().getDescription() : null,
                user.getIsPublicMatching(),
                user.getCareerDuration(),
                user.getInterestedJob(),
                user.getInterestedField(),
                careerDto,
                portfolioDto,
                projectHistoryDto,
                skillDto,
                profileType,
                tags
        );
    }

    @Transactional
    public void updateProfile(Long userId, ProfileSettingsRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if (request.profileImageUrl() != null) {
            user.setProfileImageUrl(request.profileImageUrl());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.coreCompetencies() != null) {
            user.setCoreCompetencies(request.coreCompetencies());
        }
        if (request.userStatus() != null) {
            user.setUserStatus(parseUserStatus(request.userStatus()));
        }
        if (request.isPublicMatching() != null) {
            user.setIsPublicMatching(request.isPublicMatching());
        }
        if (request.careerDuration() != null) {
            user.setCareerDuration(request.careerDuration());
        }
        if (request.interestedJob() != null) {
            user.setInterestedJob(request.interestedJob());
        }
        if (request.interestedField() != null) {
            user.setInterestedField(request.interestedField());
        }
        userRepository.save(user);

        if (request.careers() != null) {
            userCareerRepository.deleteByUserUserId(userId);
            if (!request.careers().isEmpty()) {
                List<UserCareer> newCareers = request.careers().stream()
                        .map(careerDto -> UserCareer.builder()
                                .user(user)
                                .projectName(careerDto.projectName())
                                .industryField(careerDto.industryField())
                                .startDate(careerDto.startDate())
                                .endDate(careerDto.endDate())
                                .isOngoing(careerDto.isOngoing())
                                .role(careerDto.role())
                                .build())
                        .collect(Collectors.toList());
                userCareerRepository.saveAll(newCareers);

                for (int i = 0; i < newCareers.size(); i++) {
                    UserCareer savedCareer = newCareers.get(i);
                    List<AchievementDto> achievements = request.careers().get(i).achievements();
                    if (achievements != null && !achievements.isEmpty()) {
                        List<UserAchievement> newAchievements = achievements.stream()
                                .map(achievementDto -> UserAchievement.builder()
                                        .userCareer(savedCareer)
                                        .title(achievementDto.title())
                                        .content(achievementDto.content())
                                        .build())
                                .collect(Collectors.toList());
                        userAchievementRepository.saveAll(newAchievements);
                    }
                }
            }
        }

        if (request.portfolios() != null) {
            userPortfolioRepository.deleteByUserUserId(userId);
            if (!request.portfolios().isEmpty()) {
                List<UserPortfolio> newPortfolios = request.portfolios().stream()
                        .map(portfolioDto -> UserPortfolio.builder()
                                .user(user)
                                .title(portfolioDto.title())
                                .link(portfolioDto.link())
                                .fileUrl(portfolioDto.fileUrl())
                                .build())
                        .collect(Collectors.toList());
                userPortfolioRepository.saveAll(newPortfolios);
            }
        }

        if (request.projectHistories() != null) {
            userProjectHistoryRepository.deleteByUserUserId(userId);
            if (!request.projectHistories().isEmpty()) {
                List<UserProjectHistory> newProjectHistories = request.projectHistories().stream()
                        .map(projectHistoryDto -> UserProjectHistory.builder()
                                .user(user)
                                .projectName(projectHistoryDto.projectName())
                                .projectImage(projectHistoryDto.projectImage())
                                .projectDescription(projectHistoryDto.projectDescription())
                                .startYearMonth(projectHistoryDto.startYearMonth())
                                .endYearMonth(projectHistoryDto.endYearMonth())
                                .build())
                        .collect(Collectors.toList());
                userProjectHistoryRepository.saveAll(newProjectHistories);
            }
        }
    }

    @Transactional(readOnly = true)
    public ProfileSettingsDto.ProfileAnalysisResponseDto getProfileAnalysis(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        var profileAnalysis = userProfileAnalysisRepository.findByUser(user);

        if (profileAnalysis.isEmpty()) {
            return new ProfileSettingsDto.ProfileAnalysisResponseDto(null, null);
        }

        String profileType = profileAnalysis.get().getProfileType();
        List<String> tags = null;

        if (profileAnalysis.get().getTags() != null) {
            try {
                tags = objectMapper.readValue(profileAnalysis.get().getTags(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (Exception e) {
                tags = null;
            }
        }

        return new ProfileSettingsDto.ProfileAnalysisResponseDto(profileType, tags);
    }

    private UserStatus parseUserStatus(String userStatusStr) {
        try {
            return UserStatus.valueOf(userStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserStatusException("유효하지 않은 사용자 상태입니다: " + userStatusStr);
        }
    }
}