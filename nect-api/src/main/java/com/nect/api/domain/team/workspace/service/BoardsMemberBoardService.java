package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.workspace.dto.res.MemberBoardResDto;
import com.nect.api.domain.team.workspace.dto.res.RoleFieldDto;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.team.workspace.ProjectUserWorkDaily;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import com.nect.core.repository.team.workspace.ProjectUserWorkDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoardsMemberBoardService {
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProcessRepository processRepository;
    private final ProjectUserWorkDailyRepository projectUserWorkDailyRepository;

    // 멤버 보드 조회 서비스
    @Transactional(readOnly = true)
    public MemberBoardResDto getMemberBoard(Long projectId, Long userId) {

        // 프로젝트 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        // 멤버 검증
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // 멤버 기본정보 조회
        List<ProjectUserRepository.MemberBoardRow> memberRows =
                projectUserRepository.findActiveMemberBoardRows(projectId);

        // 멤버별 "담당 프로세스" 상태 카운트 집계
        List<ProcessRepository.MemberProcessCountRow> countRows =
                processRepository.aggregateMemberProcessCounts(projectId);

        // userId -> [before, inProgress, done]
        Map<Long, long[]> countsMap = new HashMap<>();
        for (ProcessRepository.MemberProcessCountRow r : countRows) {
            Long uid = r.getUserId();
            long cnt = (r.getCnt() == null) ? 0L : r.getCnt();
            ProcessStatus status = r.getStatus();

            long[] arr = countsMap.computeIfAbsent(uid, k -> new long[]{0L, 0L, 0L});

            if (status == ProcessStatus.PLANNING) arr[0] += cnt;          // 진행 전
            else if (status == ProcessStatus.IN_PROGRESS) arr[1] += cnt; // 진행 중
            else if (status == ProcessStatus.DONE) arr[2] += cnt;        // 완료
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<ProjectUserWorkDaily> workRows =
                projectUserWorkDailyRepository.findAllByProjectIdAndWorkDate(projectId, today);

        Map<Long, ProjectUserWorkDaily> workMap = new HashMap<>();
        for (ProjectUserWorkDaily w : workRows) {
            workMap.put(w.getUser().getUserId(), w);
        }

        List<MemberBoardResDto.MemberDto> members = memberRows.stream().map(m -> {
            long[] arr = countsMap.getOrDefault(m.getUserId(), new long[]{0L, 0L, 0L});
            ProjectUserWorkDaily work = workMap.get(m.getUserId());

            boolean isWorking = (work != null && work.isWorking());
            LocalDateTime workingStartedAt = (isWorking ? work.getStartedAt() : null);

            long accumulated = (work != null && work.getAccumulatedSeconds() != null) ? work.getAccumulatedSeconds() : 0L;

            long running = 0L;
            if (isWorking && workingStartedAt != null) {
                long delta = Duration.between(workingStartedAt, now).getSeconds();
                if (delta > 0) running = delta;
            }

            long todayWorkSeconds = accumulated + running;

            RoleFieldDto fieldDto = (m.getRoleField() == RoleField.CUSTOM)
                    ? RoleFieldDto.of(m.getRoleField(), m.getCustomRoleFieldName())
                    : RoleFieldDto.of(m.getRoleField());

            return new MemberBoardResDto.MemberDto(
                    m.getUserId(),
                    m.getName(),
                    m.getNickname(),
                    null, // profile_image_url (TODO)
                    fieldDto,
                    m.getMemberType(),
                    new MemberBoardResDto.CountsDto(arr[0], arr[1], arr[2]),
                    isWorking,
                    todayWorkSeconds,
                    workingStartedAt
            );
        }).toList();

        return new MemberBoardResDto(members);
    }
}
