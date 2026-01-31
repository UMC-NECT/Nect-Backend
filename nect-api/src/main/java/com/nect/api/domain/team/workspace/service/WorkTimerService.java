package com.nect.api.domain.team.workspace.service;

import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.workspace.ProjectUserWorkDaily;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.team.workspace.ProjectUserWorkDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkTimerService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserWorkDailyRepository workDailyRepository;

    // 작업 시작
    @Transactional
    public void start(Long projectId, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        ProjectUserWorkDaily work = workDailyRepository.findTodayForUpdate(projectId, userId, today)
                .orElseGet(() -> {
                    Project project = projectRepository.getReferenceById(projectId);
                    User user = userRepository.getReferenceById(userId);
                    return workDailyRepository.save(
                            ProjectUserWorkDaily.builder()
                                    .project(project)
                                    .user(user)
                                    .workDate(today)
                                    .build()
                    );
                });

        work.start(now);
    }

    // 작업 중지
    @Transactional
    public void stop(Long projectId, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        ProjectUserWorkDaily work = workDailyRepository.findTodayForUpdate(projectId, userId, today)
                .orElse(null);

        if (work == null) return;
        work.stop(now);
    }
}
