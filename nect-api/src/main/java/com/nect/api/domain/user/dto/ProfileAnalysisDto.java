package com.nect.api.domain.user.dto;

import com.nect.api.global.ai.dto.OnboardingAnalysisScheme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProfileAnalysisDto {

    // AI 분석 결과
    private String profileType;
    private List<String> tags;
    private OnboardingAnalysisScheme.CollaborationStyle collaborationStyle;
    private List<OnboardingAnalysisScheme.SkillCategory> skills;
    private OnboardingAnalysisScheme.RoleRecommendation roleRecommendation;
    private List<OnboardingAnalysisScheme.GrowthGuide> growthGuide;

    // 추천 정보
    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecommendedProjectInfo {
        private Long projectId;
        private String projectTitle;
        private String recruitmentPeriod;
        private String recruitmentStatus;
        private String description;
        private List<String> participantRoles;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecommendedTeamMemberInfo {
        private Long userId;
        private String nickname;
        private String role;
        private String bio;
        private boolean matched;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PaginatedResponse<T> {
        private List<T> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;

        public static <T> PaginatedResponse<T> from(Page<T> page) {
            return PaginatedResponse.<T>builder()
                    .content(page.getContent())
                    .pageNumber(page.getNumber())
                    .pageSize(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .build();
        }
    }
}