package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.workspace.dto.res.MissionProgressResDto;
import com.nect.api.domain.team.workspace.dto.res.RoleFieldDto;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectTeamRoleRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardsMissionProgressService {
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;
    private final ProjectTeamRoleRepository projectTeamRoleRepository;

    record Key(RoleField roleField, String customName) {}

    @Transactional(readOnly = true)
    public MissionProgressResDto getMissionProgress(Long projectId, Long userId) {

        // 프로젝트 + 멤버 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));


        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        List<ProcessRepository.MissionProgressRow> rows = processRepository.aggregateMissionProgress(projectId);

        // 전체 합계 먼저 계산
        long totalTotal = rows.stream().mapToLong(r -> nvl(r.getTotalCount())).sum();
        long totalCompleted = rows.stream().mapToLong(r -> nvl(r.getCompletedCount())).sum();

        // 집계
        var aggMap = rows.stream().collect(Collectors.toMap(
                r -> new Key(r.getRoleField(), normalizeCustom(r.getRoleField(), r.getCustomFieldName())),
                r -> r,
                (a, b) -> a
        ));

        // ProjectTeamRole 최대 6개까지만 조회됨
        List<ProjectTeamRole> defs = projectTeamRoleRepository.findActiveOrderedForMissionProgress(projectId)
                .stream()
                .limit(6)
                .toList();

        // defs를 teams로 변환 (없으면 0/0)
        List<MissionProgressResDto.TeamDto> teams = defs.stream().map(def -> {
            RoleField rf = def.getRoleField();
            String cn = normalizeCustom(rf, def.getCustomRoleFieldName());

            ProcessRepository.MissionProgressRow row = aggMap.get(new Key(rf, cn));

            long t = (row == null) ? 0L : nvl(row.getTotalCount());
            long c = (row == null) ? 0L : nvl(row.getCompletedCount());

            return new MissionProgressResDto.TeamDto(
                    RoleFieldDto.of(rf, cn),
                    t,
                    c,
                    t <= 0 ? 0.0 : (double) c / (double) t
            );
        }).toList();

        MissionProgressResDto.TotalDto total = new MissionProgressResDto.TotalDto(
                totalTotal,
                totalCompleted,
                rate(totalCompleted, totalTotal)
        );

        return new MissionProgressResDto(total, teams);
    }

    private static long nvl(Long v) { return v == null ? 0L : v; }

    private static String normalizeCustom(RoleField rf, String name) {
        if (rf != RoleField.CUSTOM) return null;
        if (name == null) return null;
        String t = name.trim();
        return t.isBlank() ? null : t;
    }

    private double rate(long completed, long total) {
        if (total <= 0) return 0.0;
        return (double) completed / (double) total;
    }
}
