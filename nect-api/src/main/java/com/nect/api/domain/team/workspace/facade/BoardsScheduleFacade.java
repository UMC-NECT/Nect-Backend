package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.workspace.dto.req.ScheduleCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.ScheduleUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.CalendarMonthIndicatorsResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleCreateResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleGetResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleUpcomingResDto;
import com.nect.api.domain.team.workspace.service.BoardsScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardsScheduleFacade {
    private final BoardsScheduleService service;

    public CalendarMonthIndicatorsResDto getMonthIndicators(Long projectId, Long userId, int year, int month) {
        return service.getMonthIndicators(projectId, userId, year, month);
    }

    public ScheduleUpcomingResDto getUpcoming(Long projectId, Long userId, String from, int limit) {
        return service.getUpcoming(projectId, userId, from, limit);
    }

    public ScheduleCreateResDto create(Long projectId, Long userId, ScheduleCreateReqDto req) {
        return service.create(projectId, userId, req);
    }

    public void update(Long projectId, Long userId, Long scheduleId, ScheduleUpdateReqDto req) {
        service.update(projectId, userId, scheduleId, req);
    }

    public void delete(Long projectId, Long userId, Long scheduleId) {
        service.delete(projectId, userId, scheduleId);
    }
}
