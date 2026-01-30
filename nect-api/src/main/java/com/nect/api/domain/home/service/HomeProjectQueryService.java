package com.nect.api.domain.home.service;

import com.nect.api.domain.home.dto.HomeProjectMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectTeamMembers;
import com.nect.api.domain.home.dto.HomeProjectMemberItem;
import com.nect.api.domain.home.dto.HomeRecruitingProjectLeader;
import com.nect.api.domain.home.dto.HomeRecruitingProjectPosition;
import com.nect.api.domain.home.dto.HomeRecruitingProjectResponse;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.exception.CustomException;
import com.nect.core.entity.matching.Recruitment;
import com.nect.core.entity.team.Project;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HomeProjectQueryService {

    private final ProjectRepository projectRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final ProjectUserRepository projectUserRepository;

    @Transactional(readOnly = true)
    public HomeRecruitingProjectResponse getProjectInfo(Long projectId, Long userId) {

        // 필수 파라미터 체크
        if (projectId == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // 소개글은 description 우선, 없으면 information 사용
        String introduction = project.getDescription();
        if (introduction == null || introduction.isBlank()) {
            introduction = project.getInformation();
        }

        // 프로젝트 시작/종료일 계산
        LocalDate startDate = (project.getCreatedAt() == null) ? null : project.getCreatedAt().toLocalDate();
        LocalDate endDate = (project.getEndedAt() == null) ? null : project.getEndedAt().toLocalDate();

        // 마감일까지 남은 일수 계산
        Integer dDay = (project.getEndedAt() == null) ? null
                : (int) Math.max(ChronoUnit.DAYS.between(LocalDate.now(), project.getEndedAt().toLocalDate()), 0);

        // 로그인 유저가 참여 중인지 여부
        Boolean isMatching = (userId != null) && projectUserRepository.existsByProjectIdAndUserId(projectId, userId);

        // 모집 포지션 정보 조회 및 변환
        List<Recruitment> recruitments = recruitmentRepository.findAllByProjectIds(List.of(projectId));
        List<HomeRecruitingProjectPosition> positions = recruitments.stream()
                .map(recruitment -> new HomeRecruitingProjectPosition(
                        String.valueOf(recruitment.getFieldId()),
                        recruitment.getCapacity(),
                        List.of()
                ))
                .toList();

        // 리더 정보 조회(집계 결과 활용)
        HomeRecruitingProjectLeader leader = null;
        List<ProjectUserRepository.ProjectHomeStat> stats = projectUserRepository.findProjectHomeStats(List.of(projectId));
        if (!stats.isEmpty()) {
            ProjectUserRepository.ProjectHomeStat stat = stats.get(0);
            if (stat != null && stat.getLeaderUserId() != null) {
                leader = new HomeRecruitingProjectLeader(
                        stat.getLeaderUserId(),
                        stat.getLeaderName(),
                        stat.getLeaderFieldId() == null ? null : String.valueOf(stat.getLeaderFieldId()),
                        null,
                        null
                );
            }
        }

        // 상세 응답 DTO 구성
        return new HomeRecruitingProjectResponse(
                project.getId(),
                project.getTitle(),
                introduction,
                startDate,
                endDate,
                project.getRecruitmentStatus().name(),
                isMatching,
                dDay,
                null,
                List.of(),
                positions,
                null,
                List.of(),
                List.of(),
                List.of(),
                leader,
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public HomeProjectMembersResponse getMembersInfo(Long projectId) {
        // 필수 파라미터 체크
        if (projectId == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }

        // 프로젝트 존재 여부 확인
        projectRepository.findById(projectId).orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // 프로젝트 멤버 조회 및 파트별 그룹핑
        List<ProjectUserRepository.ProjectMemberInfo> memberInfos =
                projectUserRepository.findProjectMemberInfos(projectId);

        Map<String, List<HomeProjectMemberItem>> parts = new LinkedHashMap<>();
        for (ProjectUserRepository.ProjectMemberInfo info : memberInfos) {
            String partKey = (info.getFieldId() == null) ? "UNKNOWN" : String.valueOf(info.getFieldId());
            String roleInPart = (info.getMemberType() == null) ? null : info.getMemberType().name();
            String matchingStatus = "MATCHED";

            HomeProjectMemberItem item = new HomeProjectMemberItem(
                    info.getUserId(),
                    info.getName(),
                    partKey,
                    roleInPart,
                    matchingStatus,
                    null,
                    null
            );

            parts.computeIfAbsent(partKey, key -> new ArrayList<>()).add(item);
        }

        return new HomeProjectMembersResponse(
                projectId,
                new HomeProjectTeamMembers(parts)
        );
    }

}
