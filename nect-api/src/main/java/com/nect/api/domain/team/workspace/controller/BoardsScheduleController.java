package com.nect.api.domain.team.workspace.controller;


import com.nect.api.domain.team.workspace.dto.req.ScheduleCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.ScheduleUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.CalendarMonthIndicatorsResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleCreateResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleUpcomingResDto;
import com.nect.api.domain.team.workspace.facade.BoardsScheduleFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/boards")
public class BoardsScheduleController {
    private final BoardsScheduleFacade facade;

    /**
     * 캘린더 월간 인디케이터 조회
     * - 팀보드 우측 상단 캘린더에서 "해당 월에 일정이 있는 날짜"에 점/표시를 찍기 위한 API
     * - year, month 범위에 포함되는(또는 걸쳐있는) 일정들을 날짜 단위로 펼쳐서
     *      date별 event_count를 내려줌 (멀티데이 일정도 포함)
     *
     */
    @GetMapping("/calendar/month")
    public ApiResponse<CalendarMonthIndicatorsResDto> getMonthIndicators(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam int year,
            @RequestParam int month
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(facade.getMonthIndicators(projectId, userId, year, month));
    }

    // 다가오는 팀 일정 조회
    @GetMapping("/schedules/upcoming")
    public ApiResponse<ScheduleUpcomingResDto> getUpcoming(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String from,
            @RequestParam(defaultValue = "6") int limit
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(facade.getUpcoming(projectId, userId, from, limit));
    }

    // 팀 일정 생성
    @PostMapping("/schedules")
    public ApiResponse<ScheduleCreateResDto> create(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ScheduleCreateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(facade.create(projectId, userId, req));
    }

    // 팀 일정 수정
    @PatchMapping("/schedules/{scheduleId}")
    public ApiResponse<Void> update(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ScheduleUpdateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        facade.update(projectId, userId, scheduleId, req);
        return ApiResponse.ok(null);
    }

    // 팀 일정 삭제
    @DeleteMapping("/schedules/{scheduleId}")
    public ApiResponse<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        facade.delete(projectId, userDetails.getUserId(), scheduleId);
        return ApiResponse.ok(null);
    }
}
