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
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.user.User;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 홈 화면에서 필요한 프로젝트/멤버 데이터를 조회하는 서비스입니다.
 *
 * - 모집 중인 프로젝트 조회
 * - 매칭 가능한 멤버 조회
 *
 * 조회에 필요한 최소한의 집계/가공만 수행하며,
 * 추천 로직은 HomeRecommendService에서 처리됩니다.
 */
@Service
@RequiredArgsConstructor
public class HomeQueryService implements HomeService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;

    // 모집 중인 프로젝트
    @Transactional(readOnly = true)
    public HomeProjectResponse getProjects(Long userId, Integer count){

        if (userId == null || count == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }

        // 요청 개수가 0 이하이면 예외 처리
        if (count < 1) {
            throw new CustomException(HomeErrorCode.INVALID_HOME_COUNT);
        }

        // 모집 중이며 아직 참여하지 않은 프로젝트 목록 조회
        PageRequest pageRequest = PageRequest.of(0, count);
        List<Project> projects = projectRepository.findHomeProjects(userId, RecruitmentStatus.OPEN, pageRequest);

        if (projects.isEmpty()) {
            return new HomeProjectResponse(List.of());
        }

        // 이후 조회를 위한 프로젝트 ID 목록
        List<Long> projectIds = projects.stream()
                .map(Project::getId)
                .toList();

        // 프로젝트별 리더/멤버 수를 한번에 집계
        Map<Long, ProjectUserRepository.ProjectHomeStat> statsByProjectId = projectUserRepository
                .findProjectHomeStats(projectIds)
                .stream()
                .collect(Collectors.toMap(ProjectUserRepository.ProjectHomeStat::getProjectId, stat -> stat));

        // 프로젝트별 모집 포지션(roles) 및 남은 인원 합산
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

        List<HomeProjectItem> items = projects.stream()
                .map(project -> {
                    Long projectId = project.getId();
                    ProjectUserRepository.ProjectHomeStat stat = statsByProjectId.get(projectId);

                    // 리더 이름/파트 구성 (Field 연동 전이라 fieldId 문자열 사용)
                    String authorName = (stat == null) ? null : stat.getLeaderName();
                    String authorPart = (stat == null || stat.getLeaderFieldId() == null) ? null : String.valueOf(stat.getLeaderFieldId());

                    // 소개글은 description 우선, 없으면 information 사용
                    String introduction = project.getDescription();
                    if (introduction == null || introduction.isBlank()) {
                        introduction = project.getInformation();
                    }

                    // 현재 인원/모집 가능 인원 합산
                    Integer curMemberCount = (stat == null || stat.getActiveMemberCount() == null) ? 0 : stat.getActiveMemberCount().intValue();
                    Integer maxMemberCount = curMemberCount + remainingByProjectId.getOrDefault(projectId, 0);

                    // 마감일까지 남은 일수 계산 (종료일 없으면 null)
                    Integer leftDays = null;
                    if (project.getEndedAt() != null) {
                        long days = ChronoUnit.DAYS.between(LocalDate.now(), project.getEndedAt().toLocalDate());
                        leftDays = (int) Math.max(days, 0);
                    }

                    // 모집 포지션 맵 (key=fieldId 문자열)
                    // TODO: roles의 key를 파트명으로 변경
                    Map<String, Integer> roles = rolesByProjectId.getOrDefault(projectId, Collections.emptyMap());

                    return new HomeProjectItem(
                            projectId,
                            null, // TODO: 프로젝트 이미지 넣기
                            project.getTitle(),
                            authorName,
                            authorPart,
                            introduction,
                            leftDays,
                            maxMemberCount,
                            curMemberCount,
                            false, // 스크랩 여부는 제작X
                            project.getRecruitmentStatus().name(),
                            roles
                    );
                })
                .toList();

        return new HomeProjectResponse(items);
    }

    // 매칭 가능한 유저
    @Transactional(readOnly = true)
    public HomeMembersResponse getMembers(Long userId, Integer count) {

        if (userId == null || count == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }

        if (count < 1) {
            throw new CustomException(HomeErrorCode.INVALID_HOME_COUNT);
        }

        // 본인을 제외한 홈화면 매칭 가능한 유저 목록 조회
        PageRequest pageRequest = PageRequest.of(0, count);
        List<User> users = userRepository.findByUserIdNot(userId, pageRequest);

        List<HomeMemberItem> items = users.stream()
                .map(user -> new HomeMemberItem(
                        user.getUserId(),
                        null, // TODO: imageUrl은 추후 연동
                        user.getName(),
                        "개발자", // part는 추후 연동
                        "나는 바보예요", // introduction은 추후 연동
                        "매칭 가능", // status는 추후 연동
                        false, // 스크랩 여부는 제작X
                        new ArrayList<>(List.of("PM", "Design")) // roles는 추후 연동
                ))
                .toList();

        return new HomeMembersResponse(items);
    }

}
