package com.nect.api.domain.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.analysis.code.enums.IdeaAnalysisErrorCode;
import com.nect.api.domain.analysis.converter.IdeaAnalysisEntityConverter;
import com.nect.api.domain.analysis.converter.IdeaAnalysisRequestConverter;
import com.nect.api.domain.analysis.converter.IdeaAnalysisResponseConverter;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisPageResponseDto;
import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.exception.IdeaAnalysisException;
import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.dto.OpenAiResponse;
import com.nect.client.openai.dto.OpenAiResponseRequest;
import com.nect.core.entity.analysis.*;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.analysis.ProjectIdeaAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class IdeaAnalysisService {

    private final OpenAiClient openAiClient;
    private final IdeaAnalysisRequestConverter requestConverter;
    private final IdeaAnalysisResponseConverter responseConverter;
    private final ProjectIdeaAnalysisRepository projectIdeaAnalysisRepository;
    private final ObjectMapper objectMapper;


    public IdeaAnalysisResponseDto analyzeProjectIdea(Long userId, IdeaAnalysisRequestDto requestDto) {

        long analysisCount = projectIdeaAnalysisRepository.countByUserId(userId);
        if (analysisCount >= 2) {
            throw new IdeaAnalysisException(IdeaAnalysisErrorCode.TOO_MANY_ANALYSIS, "아이디어 분석은 인당 최대 2개까지만 가능합니다.");
        }

        try {

            OpenAiResponseRequest openAiRequest = requestConverter.toOpenAiRequest(requestDto);
            OpenAiResponse openAiResponse = openAiClient.createResponse(openAiRequest);
            IdeaAnalysisResponseDto response = responseConverter.toIdeaAnalysisResponse(openAiResponse);

            LocalDate targetDate = requestDto.getTargetCompletionDate();
            int totalWeeks = response.getProjectDuration().getTotalWeeks();
            LocalDate startDate = targetDate.minusWeeks(totalWeeks).plusDays(1);

            response.getProjectDuration().setStartDate(startDate);
            response.getProjectDuration().setEndDate(targetDate);
            response.getProjectDuration().setDisplayText(
                    totalWeeks + "주 (" +
                            startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                            " ~ " +
                            targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                            ")"
            );


            calculateWeeklyDates(response.getWeeklyRoadmap(), startDate);
            validateRoleFieldConsistency(response);

            ProjectIdeaAnalysis analysis = createAnalysisEntity(userId, response);
            ProjectIdeaAnalysis savedAnalysis = projectIdeaAnalysisRepository.save(analysis);


            response.setAnalysisId(savedAnalysis.getId());
            return response;

        } catch (Exception e) {
            throw new IdeaAnalysisException(IdeaAnalysisErrorCode.ANALYSIS_FAILED, "AI 분석 중 예기치 못한 오류가 발생했습니다.", e);       }
    }

    /**
     * 주차별 날짜 계산
     */
    private void calculateWeeklyDates(List<IdeaAnalysisResponseDto.WeeklyRoadmap> roadmaps, LocalDate projectStartDate) {
        for (IdeaAnalysisResponseDto.WeeklyRoadmap roadmap : roadmaps) {
            int weekNumber = roadmap.getWeekNumber();
            LocalDate weekStart = projectStartDate.plusWeeks(weekNumber - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            roadmap.setWeekStartDate(weekStart);
            roadmap.setWeekEndDate(weekEnd);
            roadmap.setWeekPeriod(
                    weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                            " ~ " +
                            weekEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );
        }
    }

    /**
     * 팀 구성과 주차별 태스크의 RoleField 일관성 검증
     */
    private void validateRoleFieldConsistency(IdeaAnalysisResponseDto response) {
        Set<String> teamRoleFields = response.getTeamComposition().stream()
                .map(IdeaAnalysisResponseDto.TeamMember::getRoleField)
                .collect(Collectors.toSet());

        for (IdeaAnalysisResponseDto.WeeklyRoadmap roadmap : response.getWeeklyRoadmap()) {
            Set<String> weekRoleFields = roadmap.getRoleTasks().stream()
                    .map(IdeaAnalysisResponseDto.RoleTask::getRoleField)
                    .collect(Collectors.toSet());

            if (!teamRoleFields.equals(weekRoleFields)) {
                throw new IdeaAnalysisException(
                        IdeaAnalysisErrorCode.ANALYSIS_FAILED,
                        String.format(
                                "%d주차의 역할 구성이 팀 구성과 일치하지 않습니다. " +
                                        "팀 구성: %s, %d주차 구성: %s",
                                roadmap.getWeekNumber(),
                                teamRoleFields,
                                roadmap.getWeekNumber(),
                                weekRoleFields
                        )
                );
            }
        }
    }

    /**
     * 분석 엔티티 생성
     */
    private ProjectIdeaAnalysis createAnalysisEntity(Long userId, IdeaAnalysisResponseDto response) {

        List<String> projectNames = response.getRecommendedProjectNames();

        // 메인 분석 엔티티 생성
        ProjectIdeaAnalysis analysis = ProjectIdeaAnalysis.builder()
                .userId(userId)
                .recommendedProjectName1(projectNames.get(0))
                .recommendedProjectName2(projectNames.size() > 1 ? projectNames.get(1) : null)
                .recommendedProjectName3(projectNames.size() > 2 ? projectNames.get(2) : null)
                .projectStartDate(response.getProjectDuration().getStartDate())
                .projectEndDate(response.getProjectDuration().getEndDate())
                .totalWeeks(response.getProjectDuration().getTotalWeeks())
                .build();

        // 팀 구성 추가
        for (IdeaAnalysisResponseDto.TeamMember member : response.getTeamComposition()) {
            AnalysisTeamComposition teamComp = AnalysisTeamComposition.builder()
                    .roleField(RoleField.valueOf(member.getRoleField()))
                    .requiredCount(member.getRequiredCount())
                    .build();
            analysis.addTeamComposition(teamComp);
        }

        // 보완할 점 추가
        for (IdeaAnalysisResponseDto.ImprovementPoint point : response.getImprovementPoints()) {
            AnalysisImprovementPoint improvementPoint = AnalysisImprovementPoint.builder()
                    .pointOrder(point.getOrder())
                    .title(point.getTitle())
                    .description(point.getDescription())
                    .build();
            analysis.addImprovementPoint(improvementPoint);
        }

        // 주차별 로드맵 추가
        for (IdeaAnalysisResponseDto.WeeklyRoadmap roadmap : response.getWeeklyRoadmap()) {
            AnalysisWeeklyRoadmap weeklyRoadmap = AnalysisWeeklyRoadmap.builder()
                    .weekNumber(roadmap.getWeekNumber())
                    .weekTitle(roadmap.getWeekTitle())
                    .weekStartDate(roadmap.getWeekStartDate())
                    .weekEndDate(roadmap.getWeekEndDate())
                    .build();

            // 역할별 태스크 추가
            for (IdeaAnalysisResponseDto.RoleTask roleTask : roadmap.getRoleTasks()) {
                AnalysisRoleTask task = AnalysisRoleTask.builder()
                        .roleField(RoleField.valueOf(roleTask.getRoleField()))
                        .tasks(roleTask.getTasks())
                        .build();
                weeklyRoadmap.addRoleTask(task);
            }

            analysis.addWeeklyRoadmap(weeklyRoadmap);
        }

        return analysis;
    }

    @Transactional(readOnly = true)
    public IdeaAnalysisPageResponseDto getAnalysisPage(Long userId, int page) {

        Pageable pageable = PageRequest.of(page, 1);

        Page<ProjectIdeaAnalysis> analysisPage =
                projectIdeaAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        if (analysisPage.isEmpty()) {
            throw new IdeaAnalysisException(
                    IdeaAnalysisErrorCode.ANALYSIS_FAILED,
                    "분석서를 찾을 수 없습니다."
            );
        }

        ProjectIdeaAnalysis analysis = analysisPage.getContent().get(0);

        ProjectIdeaAnalysis detailAnalysis = projectIdeaAnalysisRepository
                .findByIdAndUserIdWithDetails(analysis.getId(), userId)
                .orElseThrow(() -> new IdeaAnalysisException(
                        IdeaAnalysisErrorCode.ANALYSIS_FAILED,
                        "분석서를 찾을 수 없습니다."
                ));


        IdeaAnalysisResponseDto analysisDto = IdeaAnalysisEntityConverter.toDetailDto(detailAnalysis);

        return IdeaAnalysisEntityConverter.toPageResponseDto(analysisPage, analysisDto);

    }

    @Transactional
    public void deleteAnalysis(Long userId, Long analysisId) {


        ProjectIdeaAnalysis analysis = projectIdeaAnalysisRepository
                .findById(analysisId)
                .orElseThrow(() -> new IdeaAnalysisException(
                        IdeaAnalysisErrorCode.ANALYSIS_FAILED,
                        "분석서를 찾을 수 없습니다."
                ));


        if (!analysis.getUserId().equals(userId)) {
            throw new IdeaAnalysisException(
                    IdeaAnalysisErrorCode.TOO_MANY_ANALYSIS,//TODO 에러 코드 일괄수정 필요
                    "본인의 분석서만 삭제할 수 있습니다."
            );
        }


        projectIdeaAnalysisRepository.delete(analysis);
    }

}