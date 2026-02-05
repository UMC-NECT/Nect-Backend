package com.nect.api.domain.team.project.service;

import com.nect.api.domain.analysis.dto.req.ProjectCreateRequestDto;
import com.nect.api.domain.analysis.dto.res.ProjectCreateResponseDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.domain.user.service.UserService;
import com.nect.core.entity.analysis.*;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectStatus;
import com.nect.core.entity.team.enums.ProjectStatus;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.user.User;
import com.nect.core.repository.analysis.*;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserService userService;
    private final ProjectIdeaAnalysisRepository analysisRepository;
    private final ProjectTeamRoleRepository teamRoleRepository;
    private final ProjectWeeklyPlanRepository weeklyPlanRepository;
    private final ProjectWeeklyTaskRepository weeklyTaskRepository;
    private final ProjectImprovementPointRepository improvementPointRepository;


    public Project getProject(Long projectId){
        return projectRepository.findById(projectId)
                .orElseThrow(
                        () -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND)
                );
    }

    public User getLeader(Project project){
        Long userId = projectUserRepository.findLeaderByProject(project);

        return userService.getUser(userId);
    }

    public List<Project> getProjectsAsLeader(Long userId){
        return projectUserRepository.findProjectsAsLeader(userId);
    }

    public long getUserNumberOfProject(Project project){
        return projectUserRepository.countProjectUserByMemberStatusAndProject(
                ProjectMemberStatus.ACTIVE,
                project
        );
    }

    @Transactional
    public ProjectCreateResponseDto createProjectFromAnalysis(Long userId, ProjectCreateRequestDto request) {

        log.info("=== 프로젝트 생성 시작 ===");
        log.info("분석서 ID: {}, 사용자 ID: {}", request.analysisId(), userId);

        // 1. 분석서 조회 및 검증
        ProjectIdeaAnalysis analysis = analysisRepository
                .findByIdAndUserIdWithDetails(request.analysisId(), userId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.ANALYSIS_NOT_FOUND));

        // 2. 분석서 데이터 검증
        validateAnalysisData(analysis);

        // 3. Project 생성
        Project project = createProject(analysis);
        log.info("프로젝트 생성 완료: ID={}, 제목={}", project.getId(), project.getTitle());

        // 4. 팀 구성 복사
        saveTeamRoles(project.getId(), analysis);

        // 5. 주차별 로드맵 복사
        saveWeeklyRoadmap(project.getId(), analysis);

        // 6. 개선점 복사
        saveImprovementPoints(project.getId(), analysis);

        log.info("=== 프로젝트 생성 완료: ID={} ===", project.getId());

        return ProjectCreateResponseDto.of(project.getId(), project.getTitle());
    }

    private void validateAnalysisData(ProjectIdeaAnalysis analysis) {
        if (analysis.getRecommendedProjectName1() == null || analysis.getRecommendedProjectName1().isBlank()) {
            log.error("분석서 ID {}의 프로젝트명이 유효하지 않음", analysis.getId());
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }

        if (analysis.getTeamCompositions() == null || analysis.getTeamCompositions().isEmpty()) {
            log.error("분석서 ID {}의 팀 구성이 비어있음", analysis.getId());
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }

        if (analysis.getWeeklyRoadmaps() == null || analysis.getWeeklyRoadmaps().isEmpty()) {
            log.error("분석서 ID {}의 주차별 로드맵이 비어있음", analysis.getId());
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }

        log.debug("분석서 데이터 검증 완료: ID={}", analysis.getId());
    }

    /**
     * Project 엔티티 생성
     */
    private Project createProject(ProjectIdeaAnalysis analysis) {
        try {
            Project project = Project.builder()
                    .title(analysis.getRecommendedProjectName1())
                    .description("AI 분석 기반으로 생성된 프로젝트입니다") //TODO 분석서에서 마땅한 값 고려중
                    .status(ProjectStatus.ACTIVE)
                    .build();

            setRecruitmentStatus(project, RecruitmentStatus.OPEN);
            return projectRepository.save(project);
        } catch (Exception e) {
            log.error("프로젝트 생성 실패: 분석서 ID={}", analysis.getId(), e);
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }

    private void saveTeamRoles(Long projectId, ProjectIdeaAnalysis analysis) {
        try {
            List<ProjectTeamRole> teamRoles = analysis.getTeamCompositions().stream()
                    .map(tc -> ProjectTeamRole.builder()
                            .projectId(projectId)
                            .roleField(tc.getRoleField())
                            .requiredCount(tc.getRequiredCount())
                            .build())
                    .collect(Collectors.toList());

            teamRoleRepository.saveAll(teamRoles);
            log.info("팀 구성 저장 완료: {}개 역할", teamRoles.size());
        } catch (Exception e) {
            log.error("팀 구성 저장 실패: 프로젝트 ID={}", projectId, e);
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }

    private void saveWeeklyRoadmap(Long projectId, ProjectIdeaAnalysis analysis) {
        try {
            analysis.getWeeklyRoadmaps().forEach(roadmap -> {
                ProjectWeeklyPlan plan = ProjectWeeklyPlan.builder()
                        .projectId(projectId)
                        .weekNumber(roadmap.getWeekNumber())
                        .weekTitle(roadmap.getWeekTitle())
                        .weekStartDate(roadmap.getWeekStartDate())
                        .weekEndDate(roadmap.getWeekEndDate())
                        .build();

                ProjectWeeklyPlan savedPlan = weeklyPlanRepository.save(plan);

                List<ProjectWeeklyTask> tasks = roadmap.getRoleTasks().stream()
                        .map(rt -> ProjectWeeklyTask.builder()
                                .weeklyPlanId(savedPlan.getId())
                                .roleField(rt.getRoleField())
                                .tasks(rt.getTasks())
                                .build())
                        .collect(Collectors.toList());

                weeklyTaskRepository.saveAll(tasks);
                log.info("{}주차 로드맵 저장 완료: {}개 역할 태스크", roadmap.getWeekNumber(), tasks.size());
            });
        } catch (Exception e) {
            log.error("주차별 로드맵 저장 실패: 프로젝트 ID={}", projectId, e);
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }

    private void saveImprovementPoints(Long projectId, ProjectIdeaAnalysis analysis) {
        try {
            List<ProjectImprovementPoint> points = analysis.getImprovementPoints().stream()
                    .map(ip -> ProjectImprovementPoint.builder()
                            .projectId(projectId)
                            .pointOrder(ip.getPointOrder())
                            .title(ip.getTitle())
                            .description(ip.getDescription())
                            .build())
                    .collect(Collectors.toList());

            improvementPointRepository.saveAll(points);
            log.info("개선점 저장 완료: {}개", points.size());
        } catch (Exception e) {
            log.error("개선점 저장 실패: 프로젝트 ID={}", projectId, e);
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }
    private void setRecruitmentStatus(Project project, RecruitmentStatus status) {
        try {
            Field field = Project.class.getDeclaredField("recruitmentStatus");
            field.setAccessible(true);
            field.set(project, status);
            log.debug("recruitmentStatus 설정 완료: {}", status);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("recruitmentStatus 설정 실패", e);
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }

}
