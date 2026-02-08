package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.workspace.dto.res.WorkTimerSnapshot;
import com.nect.api.domain.team.workspace.service.WorkTimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkTimerFacade {

    private final WorkTimerService workTimerService;

    public void start(Long projectId, Long userId) {
        workTimerService.start(projectId, userId);
    }

    public void stop(Long projectId, Long userId) {
        workTimerService.stop(projectId, userId);
    }

    public WorkTimerSnapshot snapshot(Long projectId, Long userId) {
        return workTimerService.getTodaySnapshot(projectId, userId);
    }
}
