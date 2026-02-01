package com.nect.api.domain.home.service;

import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeMemberItem;
import com.nect.api.domain.home.dto.HomeProjectItem;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.enums.code.HomeErrorCode;
import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.exception.CustomException;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 홈 화면 추천(랜덤) 데이터를 조회하는 서비스입니다.
 *
 * - 사용자가 속하지 않은 프로젝트 중 랜덤 추천
 * - 본인을 제외한 멤버 랜덤 추천
 *
 * 조회에 필요한 최소한의 집계/가공만 수행하며,
 * 일반 조회 로직은 HomeQueryService에서 처리됩니다.
 */
@Service
@RequiredArgsConstructor
public class HomeRecommendService implements HomeService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public HomeProjectResponse getProjects(Long userId, Integer count) {
        // 유저가 속하지 않은 프로젝트를 랜덤으로 count만큼 추천합니다.

        if (count == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }

        // 요청 개수가 0 이하이면 예외 처리
        if (count < 1) {
            throw new CustomException(HomeErrorCode.INVALID_HOME_COUNT);
        }

        // 참여하지 않은 프로젝트 목록 조회 (비로그인 시 전체)
        List<Project> projects = (userId == null)
                ? projectRepository.findAll()
                : projectRepository.findProjectsExcludingUser(userId);
        if (projects.isEmpty()) {
            return new HomeProjectResponse(List.of());
        }

        // 랜덤 추천을 위해 섞은 뒤 count만큼 샘플링
        Collections.shuffle(projects);
        List<Project> sampledProjects = projects.subList(0, Math.min(count, projects.size()));

        // 이후 조회를 위한 프로젝트 ID 목록
        List<Long> projectIds = sampledProjects.stream()
                .map(Project::getId)
                .toList();

        // 프로젝트별 리더/멤버 수를 한번에 집계
        Map<Long, ProjectUserRepository.ProjectHomeStat> statsByProjectId = projectUserRepository
                .findProjectHomeStats(projectIds)
                .stream()
                .collect(Collectors.toMap(ProjectUserRepository.ProjectHomeStat::getProjectId, stat -> stat));

        // 프로젝트별 모집 포지션 및 남은 인원 합산
        List<Recruitment> recruitments = recruitmentRepository.findAllByProjectIds(projectIds);
        Map<Long, Map<String, Integer>> rolesByProjectId = new HashMap<>();
        Map<Long, Integer> remainingByProjectId = new HashMap<>();
        for (Recruitment recruitment : recruitments) {
            Long projectId = recruitment.getProject().getId();
            String roleKey = String.valueOf(recruitment.getFieldId());
            rolesByProjectId
                    .computeIfAbsent(projectId, key -> new HashMap<>())
                    .put(roleKey, recruitment.getCapacity());
            remainingByProjectId.merge(projectId, recruitment.getCapacity(), Integer::sum);
        }

        // 응답 DTO로 매핑
        List<HomeProjectItem> items = sampledProjects.stream()
                .map(project -> {
                    Long projectId = project.getId();
                    ProjectUserRepository.ProjectHomeStat stat = statsByProjectId.get(projectId);

                    String authorName = (stat == null) ? null : stat.getLeaderName();
                    String authorPart = (stat == null || stat.getLeaderFieldId() == null) ? null : String.valueOf(stat.getLeaderFieldId());

                    String introduction = project.getDescription();
                    if (introduction == null || introduction.isBlank()) {
                        introduction = project.getInformation();
                    }

                    Integer curMemberCount = (stat == null || stat.getActiveMemberCount() == null) ? 0 : stat.getActiveMemberCount().intValue();
                    Integer maxMemberCount = curMemberCount + remainingByProjectId.getOrDefault(projectId, 0);

                    Integer leftDays = null;
                    if (project.getEndedAt() != null) {
                        long days = ChronoUnit.DAYS.between(LocalDate.now(), project.getEndedAt().toLocalDate());
                        leftDays = (int) Math.max(days, 0);
                    }

                    Map<String, Integer> roles = rolesByProjectId.getOrDefault(projectId, Collections.emptyMap());

                    return new HomeProjectItem(
                            projectId,
                            null,
                            project.getTitle(),
                            authorName,
                            authorPart,
                            introduction,
                            leftDays,
                            maxMemberCount,
                            curMemberCount,
                            false,
                            project.getRecruitmentStatus().name(),
                            roles
                    );
                })
                .toList();

        return new HomeProjectResponse(items);
    }

    @Override
    @Transactional(readOnly = true)
    public HomeMembersResponse getMembers(Long userId, Integer count) {
        // 본인을 제외한 전체 멤버 중 랜덤으로 count만큼 추천합니다.
        if (count == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }

        // 요청 개수가 0 이하이면 예외 처리
        if (count < 1) {
            throw new CustomException(HomeErrorCode.INVALID_HOME_COUNT);
        }

        // 본인을 제외한 멤버 목록 조회 (비로그인 시 전체)
        List<User> users = (userId == null)
                ? userRepository.findAll()
                : userRepository.findByUserIdNot(userId);
        if (users.isEmpty()) {
            return new HomeMembersResponse(List.of());
        }

        // 랜덤 추천을 위해 섞은 뒤 count만큼 샘플링
        Collections.shuffle(users);
        List<User> sampledUsers = users.subList(0, Math.min(count, users.size()));

        // 응답 DTO로 매핑
        List<HomeMemberItem> items = sampledUsers.stream()
                .map(user -> new HomeMemberItem(
                        user.getUserId(),
                        null,
                        user.getName(),
                        "개발자",
                        "나는 바보예요",
                        "매칭 가능",
                        false,
                        new ArrayList<>(List.of("PM", "Design"))
                ))
                .toList();

        return new HomeMembersResponse(items);
    }
}
