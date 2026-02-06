package com.nect.api.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.user.dto.ProfileAnalysisDto;
import com.nect.api.global.ai.dto.OnboardingAnalysisScheme;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserProfileAnalysis;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserProfileAnalysisRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileAnalysisMatchingService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;
    private final UserProfileAnalysisRepository userProfileAnalysisRepository;
    private final UserSkillRepository userSkillRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<ProfileAnalysisDto.RecommendedProjectInfo> matchProjects(
            User user,
            OnboardingAnalysisScheme analysisResult,
            Pageable pageable) {

        try {
            List<Project> availableProjects = projectRepository.findHomeProjects(
                    user.getUserId(),
                    RecruitmentStatus.OPEN
            );

            String userRole = user.getRole() != null ? user.getRole().toString() : null;

            List<ProfileAnalysisDto.RecommendedProjectInfo> projects = availableProjects.stream()
                    .map(project -> {
                        List<String> recruitmentRoles = extractProjectRecruitmentRoles(project);
                        String recruitmentPeriod = formatRecruitmentPeriod(project);
                        return ProfileAnalysisDto.RecommendedProjectInfo.builder()
                                .projectId(project.getId())
                                .projectTitle(project.getTitle())
                                .recruitmentPeriod(recruitmentPeriod)
                                .recruitmentStatus(project.getRecruitmentStatus().getStatus())
                                .description(project.getDescription())
                                .participantRoles(recruitmentRoles)
                                .build();
                    })
                    .sorted((p1, p2) -> {
                        // 사용자 역할과 모집 역할이 일치하는 프로젝트를 우선순위로
                        boolean p1Match = userRole != null && p1.getParticipantRoles().contains(userRole);
                        boolean p2Match = userRole != null && p2.getParticipantRoles().contains(userRole);

                        if (p1Match != p2Match) {
                            return Boolean.compare(p2Match, p1Match); // 일치하는 것을 먼저
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), projects.size());

            List<ProfileAnalysisDto.RecommendedProjectInfo> pageContent =
                    projects.subList(start, end);

            return new PageImpl<>(pageContent, pageable, projects.size());

        } catch (Exception e) {
            log.error("프로젝트 매칭 중 오류 발생: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    private String formatRecruitmentPeriod(Project project) {
        if (project.getPlannedEndedOn() == null) {
            return "미정";
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = project.getPlannedEndedOn();

        if (today.isAfter(endDate)) {
            return "모집 완료";
        }

        long remainingDays = ChronoUnit.DAYS.between(today, endDate);

        if (remainingDays == 0) {
            return "D-0";
        }

        return "D-" + remainingDays;
    }

    private List<String> extractProjectRecruitmentRoles(Project project) {
        List<Recruitment> recruitments = recruitmentRepository.findOpenFieldsByProject(project);
        return recruitments.stream()
                .map(recruitment -> {
                    if (recruitment.getField().toString().equals("CUSTOM")) {
                        return recruitment.getCustomField();
                    }
                    return recruitment.getField().toString();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProfileAnalysisDto.RecommendedTeamMemberInfo> matchTeamMembers(
            User user,
            OnboardingAnalysisScheme analysisResult,
            Pageable pageable) {

        try {
            List<ProfileAnalysisDto.RecommendedTeamMemberInfo> teamMembers = userRepository.findAll().stream()
                    .filter(u -> !u.getUserId().equals(user.getUserId()))
                    .sorted((u1, u2) -> {
                        if (u1.getIsOnboardingCompleted() != u2.getIsOnboardingCompleted()) {
                            return Boolean.compare(u2.getIsOnboardingCompleted(), u1.getIsOnboardingCompleted());
                        }
                        return u1.getNickname().compareTo(u2.getNickname());
                    })
                    .map(otherUser -> ProfileAnalysisDto.RecommendedTeamMemberInfo.builder()
                                .userId(otherUser.getUserId())
                                .nickname(otherUser.getNickname())
                                .role(otherUser.getRole() != null ? otherUser.getRole().toString() : "MEMBER")
                                .bio(otherUser.getBio())
                                .matched(false)
                                .build()
                    )
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), teamMembers.size());

            List<ProfileAnalysisDto.RecommendedTeamMemberInfo> pageContent =
                    teamMembers.subList(start, end);

            return new PageImpl<>(pageContent, pageable, teamMembers.size());

        } catch (Exception e) {
            log.error("팀원 매칭 중 오류 발생: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }


    public OnboardingAnalysisScheme parseProfileAnalysis(UserProfileAnalysis analysis) {
        try {
            OnboardingAnalysisScheme scheme = new OnboardingAnalysisScheme();
            scheme.profile_type = analysis.getProfileType();

            if (analysis.getTags() != null) {
                scheme.tags = objectMapper.readValue(analysis.getTags(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }

            if (analysis.getCollaborationStyle() != null) {
                scheme.collaboration_style = objectMapper.readValue(analysis.getCollaborationStyle(),
                        OnboardingAnalysisScheme.CollaborationStyle.class);
            }

            if (analysis.getSkills() != null) {
                scheme.skills = objectMapper.readValue(analysis.getSkills(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, OnboardingAnalysisScheme.SkillCategory.class));
            }

            return scheme;
        } catch (Exception e) {
            log.warn("프로필 분석 결과 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
}