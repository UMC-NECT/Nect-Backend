package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.workspace.dto.res.WorkTimerSnapshot;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.workspace.ProjectUserWorkDaily;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.workspace.ProjectUserWorkDailyRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkTimerService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectUserWorkDailyRepository workDailyRepository;

    // 시작 서비스 로직
    @Transactional
    public void start(Long projectId, Long userId) {
        validateProjectMember(projectId, userId);

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        ProjectUserWorkDaily work = getOrCreateTodayRowForUpdate(projectId, userId, today);
        work.start(now);
    }

    // 정지 서비스 로직
    @Transactional
    public void stop(Long projectId, Long userId) {
        validateProjectMember(projectId, userId);

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        ProjectUserWorkDaily work = workDailyRepository.findTodayForUpdate(projectId, userId, today)
                .orElse(null);

        if (work == null) return;
        work.stop(now);
    }


    // 스냅샷 서비스 로직
    @Transactional(readOnly = true)
    public WorkTimerSnapshot getTodaySnapshot(Long projectId, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        ProjectUserWorkDaily work = workDailyRepository.findToday(projectId, userId, today)
                .orElse(null);

        return (work == null) ? WorkTimerSnapshot.empty() : toSnapshot(work, now);
    }



    private void validateProjectMember(Long projectId, Long userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId);
        }

        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        if (!userRepository.existsById(userId)) {
            throw new BoardsException(BoardsErrorCode.USER_NOT_FOUND, "userId=" + userId);
        }
    }


    private ProjectUserWorkDaily getOrCreateTodayRowForUpdate(Long projectId, Long userId, LocalDate today) {
        return workDailyRepository.findTodayForUpdate(projectId, userId, today)
                .orElseGet(() -> {
                    try {
                        Project projectRef = projectRepository.getReferenceById(projectId);
                        User userRef = userRepository.getReferenceById(userId);

                        ProjectUserWorkDaily created = ProjectUserWorkDaily.builder()
                                .project(projectRef)
                                .user(userRef)
                                .workDate(today)
                                .build();

                        return workDailyRepository.save(created);
                    } catch (DataIntegrityViolationException e) {
                        return workDailyRepository.findTodayForUpdate(projectId, userId, today)
                                .orElseThrow(() -> e);
                    }
                });
    }

    private WorkTimerSnapshot toSnapshot(ProjectUserWorkDaily work, LocalDateTime now) {
        boolean isWorking = work.isWorking();
        LocalDateTime startedAt = isWorking ? work.getStartedAt() : null;

        long accumulated = (work.getAccumulatedSeconds() != null) ? work.getAccumulatedSeconds() : 0L;

        long running = 0L;
        if (isWorking && startedAt != null) {
            long delta = Duration.between(startedAt, now).getSeconds();
            if (delta > 0) running = delta;
        }

        return new WorkTimerSnapshot(isWorking, accumulated + running, startedAt);
    }
}
