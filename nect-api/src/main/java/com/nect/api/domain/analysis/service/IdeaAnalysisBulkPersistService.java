package com.nect.api.domain.analysis.service;

import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.core.entity.analysis.*;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.analysis.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * 아이디어 분석 결과를 배치로 저장/갱신하는 전용 레포지토리입니다.
 *
 * 분석 결과가 대량의 하위 엔티티(팀 구성, 개선점, 주차별 로드맵, 역할별 태스크)를
 * 포함하므로, 저장 성능을 위해 batch 단위로 flush/clear를 수행합니다.
 *
 *   Repository임에도 불구하고 api 모듈에 둔 이유
 *   • IdeaAnalysisBulkRepository가 DTO(IdeaAnalysisResponseDto)를 직접 받음 → core에 두면 계층 의존이 역전됨
 *   • 여러 Repository 조합 + batch flush/clear 같은 애플리케이션 레벨 orchestration 성격이 강함
 *
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class IdeaAnalysisBulkPersistService {

    private static final int BATCH_SIZE = 50;

    private final ProjectIdeaAnalysisRepository projectIdeaAnalysisRepository;
    private final AnalysisTeamCompositionRepository teamCompositionRepository;
    private final AnalysisImprovementPointRepository improvementPointRepository;
    private final AnalysisWeeklyRoadmapRepository weeklyRoadmapRepository;
    private final AnalysisRoleTaskRepository roleTaskRepository;
    private final EntityManager entityManager;

    /**
     * 기존 분석 stub에 상세 정보를 일괄 저장합니다.
     *
     * - 메인 분석 엔티티 업데이트
     * - 하위 엔티티들을 batch로 저장
     */
    public ProjectIdeaAnalysis updateAnalysisWithDetails(Long analysisId, IdeaAnalysisResponseDto response) {
        ProjectIdeaAnalysis analysis = projectIdeaAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("analysis not found id=" + analysisId));

        List<String> projectNames = response.getRecommendedProjectNames();
        analysis.updateDetails(
                response.getDescription(),
                projectNames.get(0),
                projectNames.size() > 1 ? projectNames.get(1) : null,
                projectNames.size() > 2 ? projectNames.get(2) : null,
                response.getProjectDuration().getStartDate(),
                response.getProjectDuration().getEndDate(),
                response.getProjectDuration().getTotalWeeks()
        );
        projectIdeaAnalysisRepository.save(analysis);

        List<AnalysisTeamComposition> teamComps = new ArrayList<>();
        for (IdeaAnalysisResponseDto.TeamMember member : response.getTeamComposition()) {
            AnalysisTeamComposition teamComp = AnalysisTeamComposition.builder()
                    .roleField(RoleField.valueOf(member.getRoleField()))
                    .requiredCount(member.getRequiredCount())
                    .build();
            teamComp.setAnalysis(analysis);
            teamComps.add(teamComp);
        }

        List<AnalysisImprovementPoint> improvementPoints = new ArrayList<>();
        for (IdeaAnalysisResponseDto.ImprovementPoint point : response.getImprovementPoints()) {
            AnalysisImprovementPoint improvementPoint = AnalysisImprovementPoint.builder()
                    .pointOrder(point.getOrder())
                    .title(point.getTitle())
                    .description(point.getDescription())
                    .build();
            improvementPoint.setAnalysis(analysis);
            improvementPoints.add(improvementPoint);
        }

        List<AnalysisWeeklyRoadmap> weeklyRoadmaps = new ArrayList<>();
        List<AnalysisRoleTask> roleTasks = new ArrayList<>();
        for (IdeaAnalysisResponseDto.WeeklyRoadmap roadmap : response.getWeeklyRoadmap()) {
            AnalysisWeeklyRoadmap weeklyRoadmap = AnalysisWeeklyRoadmap.builder()
                    .weekNumber(roadmap.getWeekNumber())
                    .weekTitle(roadmap.getWeekTitle())
                    .weekStartDate(roadmap.getWeekStartDate())
                    .weekEndDate(roadmap.getWeekEndDate())
                    .build();
            weeklyRoadmap.setAnalysis(analysis);
            weeklyRoadmaps.add(weeklyRoadmap);

            for (IdeaAnalysisResponseDto.RoleTask roleTask : roadmap.getRoleTasks()) {
                AnalysisRoleTask task = AnalysisRoleTask.builder()
                        .roleField(RoleField.valueOf(roleTask.getRoleField()))
                        .tasks(roleTask.getTasks())
                        .build();
                task.setWeeklyRoadmap(weeklyRoadmap);
                roleTasks.add(task);
            }
        }

        batchSave("teamComposition", teamCompositionRepository, teamComps, true);
        batchSave("improvementPoint", improvementPointRepository, improvementPoints, true);
        batchSave("weeklyRoadmap", weeklyRoadmapRepository, weeklyRoadmaps, false);
        weeklyRoadmapRepository.flush();

        batchSave("roleTask", roleTaskRepository, roleTasks, true);
        roleTaskRepository.flush();

        return analysis;
    }

    /**
     * 하위 엔티티를 batch 단위로 저장합니다.
     *
     * flush/clear 정책을 통해 메모리 사용량과 영속성 컨텍스트 크기를 제어합니다.
     */
    private <T> void batchSave(String label, JpaRepository<T, Long> repository, List<T> items, boolean clearAfterBatch) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, items.size());
            long start = System.nanoTime();
            repository.saveAll(items.subList(i, end));
            repository.flush();
            long ms = (System.nanoTime() - start) / 1_000_000L;
            log.info("ideaAnalysisSplit batchSave label={} range={}~{} size={} ms={}", label, i + 1, end, end - i, ms);
            if (clearAfterBatch) {
                entityManager.clear();
            }
        }
    }
}
