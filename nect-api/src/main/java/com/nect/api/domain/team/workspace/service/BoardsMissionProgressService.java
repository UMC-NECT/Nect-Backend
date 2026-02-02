package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.workspace.dto.res.MissionProgressResDto;
import com.nect.api.domain.team.workspace.dto.res.RoleFieldDto;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardsMissionProgressService {
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;

    @Transactional(readOnly = true)
    public MissionProgressResDto getMissionProgress(Long projectId, Long userId) {

        // 프로젝트 + 멤버 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        List<ProcessRepository.MissionProgressRow> rows = processRepository.aggregateMissionProgress(projectId);

        // 전체 합계 먼저 계산
        long totalTotal = rows.stream()
                .mapToLong(r -> r.getTotalCount() == null ? 0L : r.getTotalCount())
                .sum();

        long totalCompleted = rows.stream()
                .mapToLong(r -> r.getCompletedCount() == null ? 0L : r.getCompletedCount())
                .sum();


        List<MissionProgressResDto.TeamDto> teams = rows.stream()
                .map(r -> {
                    long total = r.getTotalCount() == null ? 0L : r.getTotalCount();
                    long completed = r.getCompletedCount() == null ? 0L : r.getCompletedCount();

                    return new MissionProgressResDto.TeamDto(
                            RoleFieldDto.of(r.getRoleField(), r.getCustomFieldName()),
                            total,
                            completed,
                            total <= 0 ? 0.0 : (double) completed / (double) total
                    );
                })
                .toList();

        MissionProgressResDto.TotalDto total = new MissionProgressResDto.TotalDto(
                totalTotal,
                totalCompleted,
                rate(totalCompleted, totalTotal)
        );

        return new MissionProgressResDto(total, teams);
    }

    private double rate(long completed, long total) {
        if (total <= 0) return 0.0;
        return (double) completed / (double) total;
    }
}
