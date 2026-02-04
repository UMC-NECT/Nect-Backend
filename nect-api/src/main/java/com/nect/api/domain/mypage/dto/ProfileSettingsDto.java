package com.nect.api.domain.mypage.dto;

import java.time.LocalDate;
import java.util.List;

public class ProfileSettingsDto {

    public record ProfileSettingsResponseDto(
            Long userId,
            String name,
            String nickname,
            String email,
            String role,
            String profileImageUrl,
            String bio,
            String coreCompetencies,
            String userStatus,
            Boolean isPublicMatching,
            String careerDuration,
            String interestedJob,
            String interestedField,
            List<CareerDto> careers,
            List<PortfolioDto> portfolios,
            List<ProjectHistoryDto> projectHistories,
            List<SkillDto> skills
    ) {}

    public record ProfileSettingsRequestDto(
            String profileImageUrl,
            String bio,
            String coreCompetencies,
            String userStatus,
            Boolean isPublicMatching,
            String careerDuration,
            String interestedJob,
            String interestedField,
            List<CareerDto> careers,
            List<PortfolioDto> portfolios,
            List<ProjectHistoryDto> projectHistories
    ) {}

    public record CareerDto(
            Long userCareerId,
            String projectName,
            String industryField,
            String startDate,
            String endDate,
            Boolean isOngoing,
            String role,
            List<AchievementDto> achievements
    ) {}

    public record AchievementDto(
            Long userAchievementId,
            String title,
            String content
    ) {}

    public record PortfolioDto(
            Long userPortfolioId,
            String title,
            String link,
            String fileUrl
    ) {}

    public record ProjectHistoryDto(
            Long userProjectHistoryId,
            String projectName,
            String projectImage,
            String projectDescription,
            String startYearMonth,
            String endYearMonth
    ) {}

    public record SkillDto(
            String category,
            String categoryLabel,
            List<SkillItemDto> skills
    ) {}

    public record SkillItemDto(
            String skill,
            String skillLabel,
            Boolean isSelected
    ) {}
}