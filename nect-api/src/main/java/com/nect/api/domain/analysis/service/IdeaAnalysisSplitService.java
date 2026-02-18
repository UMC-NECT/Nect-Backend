package com.nect.api.domain.analysis.service;

import com.nect.api.domain.analysis.code.enums.IdeaAnalysisErrorCode;
import com.nect.api.domain.analysis.converter.IdeaAnalysisSplitRequestConverter;
import com.nect.api.domain.analysis.converter.IdeaAnalysisSplitResponseConverter;
import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.exception.IdeaAnalysisException;
import com.nect.client.openai.OpenAiClient;
import com.nect.client.openai.dto.OpenAiResponse;
import com.nect.core.entity.analysis.ProjectIdeaAnalysis;
import com.nect.core.entity.analysis.enums.AnalysisSaveStatus;
import com.nect.core.repository.analysis.ProjectIdeaAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * 분석 결과를 파트별로 생성해 병렬 처리하는 서비스입니다.
 *
 * - Part A/B/C 분리 호출
 * - 결과 병합 후 응답 반환
 * - 비동기 저장을 위한 Redis 캐시 적재 및 stub 생성
 */
@Service
@RequiredArgsConstructor
public class IdeaAnalysisSplitService {

    private static final int WEEK_CHUNK_SIZE = 4;

    private final OpenAiClient openAiClient;
    private final IdeaAnalysisSplitRequestConverter requestConverter;
    private final IdeaAnalysisSplitResponseConverter responseConverter;
    private final ProjectIdeaAnalysisRepository projectIdeaAnalysisRepository;
    private final IdeaAnalysisBulkPersistService ideaAnalysisBulkPersistService;
    private final AnalysisRedisCacheService analysisRedisCacheService;
    private final IdeaAnalysisSplitAsyncPersistService asyncPersistService;

    /**
     * 아이디어 분석을 파트 분할 방식으로 수행합니다.
     *
     * 응답은 즉시 반환하고, 저장은 Redis 캐시 기반으로 비동기 처리됩니다.
     */
    @Transactional
    public IdeaAnalysisResponseDto analyzeProjectIdeaSplit(Long userId, IdeaAnalysisRequestDto requestDto) {
        long analysisCount = projectIdeaAnalysisRepository.countByUserId(userId);
        if (analysisCount >= 2) {
            throw new IdeaAnalysisException(IdeaAnalysisErrorCode.TOO_MANY_ANALYSIS, "아이디어 분석은 인당 최대 2개까지만 가능합니다.");
        }

        try {
            CompletableFuture<IdeaAnalysisResponseDto> partAFuture = CompletableFuture.supplyAsync(() -> {
                OpenAiResponse response = openAiClient.createResponse(requestConverter.toOpenAiRequestPartA(requestDto));
                return responseConverter.toPartAResponse(response);
            });

            CompletableFuture<IdeaAnalysisResponseDto> partBFuture = CompletableFuture.supplyAsync(() -> {
                OpenAiResponse response = openAiClient.createResponse(requestConverter.toOpenAiRequestPartB(requestDto));
                return responseConverter.toPartBResponse(response);
            });

            CompletableFuture<IdeaAnalysisResponseDto> partCFuture = partAFuture.thenCombine(partBFuture, SplitDeps::new)
                    .thenApplyAsync(deps -> {
                        Integer totalWeeks = deps.partA.getProjectDuration() != null
                                ? deps.partA.getProjectDuration().getTotalWeeks()
                                : null;
                        if (totalWeeks == null) {
                            throw new IdeaAnalysisException(IdeaAnalysisErrorCode.ANALYSIS_FAILED, "total_weeks 파싱에 실패했습니다.");
                        }
                        if (deps.partB.getTeamComposition() == null || deps.partB.getTeamComposition().isEmpty()) {
                            throw new IdeaAnalysisException(IdeaAnalysisErrorCode.ANALYSIS_FAILED, "team_composition 파싱에 실패했습니다.");
                        }
                        List<CompletableFuture<IdeaAnalysisResponseDto>> chunkFutures = buildPartCFutures(
                                requestDto,
                                totalWeeks,
                                deps.partB.getTeamComposition(),
                                userId
                        );
                        CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).join();
                        List<IdeaAnalysisResponseDto.WeeklyRoadmap> merged = chunkFutures.stream()
                                .map(CompletableFuture::join)
                                .flatMap(part -> part.getWeeklyRoadmap().stream())
                                .sorted((a, b) -> Integer.compare(a.getWeekNumber(), b.getWeekNumber()))
                                .toList();
                        IdeaAnalysisResponseDto result = IdeaAnalysisResponseDto.builder()
                                .weeklyRoadmap(merged)
                                .build();
                        return result;
                    });

            IdeaAnalysisResponseDto partA = partAFuture.join();
            IdeaAnalysisResponseDto partB = partBFuture.join();
            IdeaAnalysisResponseDto partC = partCFuture.join();

            IdeaAnalysisResponseDto response = IdeaAnalysisResponseDto.builder()
                    .description(partA.getDescription())
                    .recommendedProjectNames(partA.getRecommendedProjectNames())
                    .projectDuration(partA.getProjectDuration())
                    .teamComposition(partB.getTeamComposition())
                    .improvementPoints(partB.getImprovementPoints())
                    .weeklyRoadmap(partC.getWeeklyRoadmap())
                    .build();

            finalizeAndPersist(userId, response);
            Long analysisId = createAnalysisStub(userId);
            response.setAnalysisId(analysisId);
            analysisRedisCacheService.cacheResponse(analysisId, response);
            asyncPersistService.persistFromCacheAsync(analysisId);
            return response;
        } catch (CompletionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IdeaAnalysisException(IdeaAnalysisErrorCode.ANALYSIS_FAILED, "AI 분석 중 오류가 발생했습니다.", cause);
        } catch (Exception e) {
            throw new IdeaAnalysisException(IdeaAnalysisErrorCode.ANALYSIS_FAILED, "AI 분석 중 오류가 발생했습니다.", e);
        }
    }

    private void finalizeAndPersist(Long userId, IdeaAnalysisResponseDto response) {
        LocalDate startDate = LocalDate.now();
        int totalWeeks = response.getProjectDuration().getTotalWeeks();
        LocalDate endDate = startDate.plusWeeks(totalWeeks).minusDays(1);

        response.getProjectDuration().setStartDate(startDate);
        response.getProjectDuration().setEndDate(endDate);
        response.getProjectDuration().setDisplayText(
                totalWeeks + "주 (" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                        " ~ " +
                        endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                        ")"
        );

        calculateWeeklyDates(response.getWeeklyRoadmap(), startDate);
        validateRoleFieldConsistency(response);

        // main persistence happens asynchronously after response
    }

    /**
     * 비동기 저장 전용 PENDING stub을 생성합니다.
     */
    private Long createAnalysisStub(Long userId) {
        ProjectIdeaAnalysis stub = ProjectIdeaAnalysis.builder()
                .userId(userId)
                .description(AnalysisSaveStatus.PENDING.getStatus())
                .recommendedProjectName1("PENDING")
                .build();
        return projectIdeaAnalysisRepository.save(stub).getId();
    }

    private List<CompletableFuture<IdeaAnalysisResponseDto>> buildPartCFutures(
            IdeaAnalysisRequestDto requestDto,
            int totalWeeks,
            List<IdeaAnalysisResponseDto.TeamMember> teamComposition,
            Long userId) {

        List<CompletableFuture<IdeaAnalysisResponseDto>> futures = new ArrayList<>();
        int start = 1;
        while (start <= totalWeeks) {
            int end = Math.min(start + WEEK_CHUNK_SIZE - 1, totalWeeks);
            int chunkStart = start;
            int chunkEnd = end;
            futures.add(CompletableFuture.supplyAsync(() -> {
                OpenAiResponse response = openAiClient.createResponse(
                        requestConverter.toOpenAiRequestPartC(requestDto, totalWeeks, chunkStart, chunkEnd, teamComposition));
                return responseConverter.toPartCResponse(response);
            }));
            start = end + 1;
        }
        return futures;
    }

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

    private static class SplitDeps {
        private final IdeaAnalysisResponseDto partA;
        private final IdeaAnalysisResponseDto partB;

        private SplitDeps(IdeaAnalysisResponseDto partA, IdeaAnalysisResponseDto partB) {
            this.partA = partA;
            this.partB = partB;
        }
    }
}
