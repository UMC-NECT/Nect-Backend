package com.nect.api.domain.analysis.converter;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisPageResponseDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto.*;
import com.nect.core.entity.analysis.ProjectIdeaAnalysis;
import org.springframework.data.domain.Page;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
public class IdeaAnalysisEntityConverter {


    public static IdeaAnalysisResponseDto toDetailDto(ProjectIdeaAnalysis analysis) {

        ProjectDuration projectDuration = ProjectDuration.builder()
                .startDate(analysis.getProjectStartDate())
                .endDate(analysis.getProjectEndDate())
                .totalWeeks(analysis.getTotalWeeks())
                .displayText(
                        analysis.getTotalWeeks() + "주 (" +
                                analysis.getProjectStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                " ~ " +
                                analysis.getProjectEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                ")"
                )
                .build();

        List<TeamMember> teamComposition = analysis.getTeamCompositions().stream()
                .map(tc -> TeamMember.builder()
                        .roleField(tc.getRoleField().name())
                        .roleFieldDisplayName(tc.getRoleField().getDescription())
                        .requiredCount(tc.getRequiredCount())
                        .build())
                .collect(Collectors.toList());

        List<ImprovementPoint> improvementPoints = analysis.getImprovementPoints().stream()
                .sorted((a, b) -> a.getPointOrder().compareTo(b.getPointOrder()))
                .map(ip -> ImprovementPoint.builder()
                        .order(ip.getPointOrder())
                        .title(ip.getTitle())
                        .description(ip.getDescription())
                        .build())
                .collect(Collectors.toList());

        List<WeeklyRoadmap> weeklyRoadmap = analysis.getWeeklyRoadmaps().stream()
                .sorted((a, b) -> a.getWeekNumber().compareTo(b.getWeekNumber()))
                .map(wr -> {

                    List<RoleTask> roleTasks = wr.getRoleTasks().stream()
                            .map(rt -> RoleTask.builder()
                                    .roleField(rt.getRoleField().name())
                                    .roleFieldDisplayName(rt.getRoleField().getDescription())
                                    .tasks(rt.getTasks())
                                    .build())
                            .collect(Collectors.toList());

                    return WeeklyRoadmap.builder()
                            .weekNumber(wr.getWeekNumber())
                            .weekTitle(wr.getWeekTitle())
                            .weekStartDate(wr.getWeekStartDate())
                            .weekEndDate(wr.getWeekEndDate())
                            .weekPeriod(
                                    wr.getWeekStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                            " ~ " +
                                            wr.getWeekEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            )
                            .roleTasks(roleTasks)
                            .build();
                })
                .collect(Collectors.toList());

        return IdeaAnalysisResponseDto.builder()
                .analysisId(analysis.getId())
                .recommendedProjectNames(analysis.getRecommendedProjectNames())
                .projectDuration(projectDuration)
                .teamComposition(teamComposition)
                .improvementPoints(improvementPoints)
                .weeklyRoadmap(weeklyRoadmap)
                .build();
    }

    public static IdeaAnalysisPageResponseDto toPageResponseDto(
            Page<ProjectIdeaAnalysis> analysisPage,
            IdeaAnalysisResponseDto analysisDto) {

        IdeaAnalysisPageResponseDto.PageInfo pageInfo = IdeaAnalysisPageResponseDto.PageInfo.builder()
                .currentPage(analysisPage.getNumber())
                .totalPages(analysisPage.getTotalPages())
                .totalElements(analysisPage.getTotalElements())
                .hasNext(analysisPage.hasNext())
                .hasPrevious(analysisPage.hasPrevious())
                .build();

        return IdeaAnalysisPageResponseDto.builder()
                .analysis(analysisDto)
                .pageInfo(pageInfo)
                .build();
    }
}
