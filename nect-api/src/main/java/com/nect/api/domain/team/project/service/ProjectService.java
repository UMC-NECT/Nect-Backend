package com.nect.api.domain.team.project.service;

import com.nect.api.domain.analysis.dto.req.ProjectCreateRequestDto;
import com.nect.api.domain.analysis.dto.res.ProjectCreateResponseDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.domain.user.service.UserService;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.analysis.*;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.team.process.ProcessTaskItem;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.analysis.*;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nect.core.entity.team.enums.ProjectStatus;
import com.nect.core.repository.analysis.ProjectIdeaAnalysisRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectIdeaAnalysisRepository analysisRepository;
    private final ProjectTeamRoleRepository teamRoleRepository;
    private final ProjectWeeklyPlanRepository weeklyPlanRepository;
    private final ProjectWeeklyTaskRepository weeklyTaskRepository;
    private final ProjectImprovementPointRepository improvementPointRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;
    private final UserService userService;

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

        // 1. 분석서 조회 및 검증
        ProjectIdeaAnalysis analysis = analysisRepository
                .findByIdAndUserIdWithDetails(request.analysisId(), userId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.ANALYSIS_NOT_FOUND));

        // 2. 분석서 데이터 검증
        validateAnalysisData(analysis);

        // 3. Project 생성
        Project project = createProject(analysis);

        addProjectLeader(project, userId);

        // 4. 팀 구성 복사
        saveTeamRoles(project.getId(), analysis);

        // 5. 주차별 로드맵 복사
        saveWeeklyRoadmap(project.getId(), analysis);

        // 6. 개선점 복사
        saveImprovementPoints(project.getId(), analysis);


        return ProjectCreateResponseDto.of(project.getId(), project.getTitle());
    }

    private void validateAnalysisData(ProjectIdeaAnalysis analysis) {
        if (analysis.getRecommendedProjectName1() == null || analysis.getRecommendedProjectName1().isBlank()) {
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }

        if (analysis.getTeamCompositions() == null || analysis.getTeamCompositions().isEmpty()) {
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }

        if (analysis.getWeeklyRoadmaps() == null || analysis.getWeeklyRoadmaps().isEmpty()) {
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }

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
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }

    private void saveTeamRoles(Long projectId, ProjectIdeaAnalysis analysis) {
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));


            List<ProjectTeamRole> teamRoles = analysis.getTeamCompositions().stream()
                    .map(tc -> ProjectTeamRole.builder()
                            .project(project)
                            .roleField(tc.getRoleField())
                            .requiredCount(tc.getRequiredCount())
                            .build())
                    .collect(Collectors.toList());

            teamRoleRepository.saveAll(teamRoles);
        } catch (Exception e) {
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }

    private void saveWeeklyRoadmap(Long projectId, ProjectIdeaAnalysis analysis) {
        try {
            // 프로젝트 조회
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

            // 프로젝트 리더 조회
            ProjectUser projectLeader = projectUserRepository
                    .findByProjectIdAndMemberType(projectId, ProjectMemberType.LEADER)
                    .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_USER_NOT_FOUND));

            User leader = userRepository.findById(projectLeader.getUserId())
                    .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

            analysis.getWeeklyRoadmaps().forEach(roadmap -> {

                // 1) ProjectWeeklyPlan 저장
                ProjectWeeklyPlan plan = ProjectWeeklyPlan.builder()
                        .projectId(projectId)
                        .weekNumber(roadmap.getWeekNumber())
                        .weekTitle(roadmap.getWeekTitle())
                        .weekStartDate(roadmap.getWeekStartDate())
                        .weekEndDate(roadmap.getWeekEndDate())
                        .build();

                ProjectWeeklyPlan savedPlan = weeklyPlanRepository.save(plan);

                // 2) ProjectWeeklyTask 저장
                List<ProjectWeeklyTask> tasks = roadmap.getRoleTasks().stream()
                        .map(rt -> ProjectWeeklyTask.builder()
                                .weeklyPlanId(savedPlan.getId())
                                .roleField(rt.getRoleField())
                                .tasks(rt.getTasks())
                                .build())
                        .collect(Collectors.toList());

                weeklyTaskRepository.saveAll(tasks);

                // 3) WEEK_MISSION Process 생성
                Process mission = Process.builder()
                        .project(project)
                        .createdBy(leader)
                        .title(roadmap.getWeekTitle())
                        .content(null)
                        .build();

                mission.markAsWeekMission(roadmap.getWeekNumber());
                mission.updatePeriod(roadmap.getWeekStartDate(), roadmap.getWeekEndDate());

                // 4) 역할별 체크리스트 아이템 생성
                roadmap.getRoleTasks().forEach(roleTask -> {
                    List<String> items = parseTasks(roleTask.getTasks());

                    for (int i = 0; i < items.size(); i++) {
                        mission.addTaskItem(ProcessTaskItem.builder()
                                .content(items.get(i))
                                .isDone(false)
                                .sortOrder(i)
                                .roleField(roleTask.getRoleField())
                                .customRoleFieldName(null)
                                .build());
                    }
                });

                processRepository.save(mission);

            });

        } catch (Exception e) {
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }

    private List<String> parseTasks(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String normalized = raw.replace("\r\n", "\n").trim();

        List<String> lines = java.util.Arrays.stream(normalized.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (lines.size() == 1) {
            return java.util.Arrays.stream(lines.get(0).split("[,·/]|\\s{2,}"))
                    .map(String::trim)
                    .map(s -> s.replaceFirst("^[0-9]+[\\.|\\)]\\s*", ""))
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        return lines;
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
        } catch (Exception e) {
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }
    private void setRecruitmentStatus(Project project, RecruitmentStatus status) {
        try {
            Field field = Project.class.getDeclaredField("recruitmentStatus");
            field.setAccessible(true);
            field.set(project, status);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ProjectException(ProjectErrorCode.INVALID_ANALYSIS_DATA);
        }
    }
    private void addProjectLeader(Project project, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));


        ProjectUser projectUser = ProjectUser.builder()
                .project(project)
                .userId(userId)
                .memberType(ProjectMemberType.LEADER)
                .memberStatus(ProjectMemberStatus.ACTIVE)
                .roleField(RoleField.SERVICE)
                .customRoleFieldName(null)
                .build();

        projectUserRepository.save(projectUser);

    }

}
