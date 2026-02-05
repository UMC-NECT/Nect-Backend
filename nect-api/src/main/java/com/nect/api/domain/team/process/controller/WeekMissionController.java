package com.nect.api.domain.team.process.controller;

import com.nect.api.domain.team.process.dto.req.WeekMissionStatusUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.WeekMissionTaskItemUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemResDto;
import com.nect.api.domain.team.process.dto.res.WeekMissionDetailResDto;
import com.nect.api.domain.team.process.dto.res.WeekMissionWeekResDto;
import com.nect.api.domain.team.process.service.WeekMissionService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/week-missions")
public class WeekMissionController {

    private final WeekMissionService weekMissionService;

    // 주차별 위크미션 조회
    @GetMapping("/week")
    public ApiResponse<WeekMissionWeekResDto> getWeekMissions(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "weeks", required = false, defaultValue = "1") Integer weeks
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(weekMissionService.getWeekMissions(projectId, userId, startDate, weeks));
    }

    // 위크미션 상세 조회(체크리스트 포함)
    @GetMapping("/{processId}")
    public ApiResponse<WeekMissionDetailResDto> getWeekMissionDetail(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.ok(
                weekMissionService.getDetail(projectId, userDetails.getUserId(), processId)
        );
    }

    // 위크미션 상태 변경
    @PatchMapping("/{processId}/status")
    public ApiResponse<Void> updateWeekMissionStatus(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody WeekMissionStatusUpdateReqDto req
    ) {
        weekMissionService.updateWeekMissionStatus(projectId, userDetails.getUserId(), processId, req);
        return ApiResponse.ok(null);
    }

    // 위크미션 TASK 내 항목 내용 수정
    @PatchMapping("/{processId}/task-items/{taskItemId}")
    public ApiResponse<ProcessTaskItemResDto> updateWeekMissionTaskItem(
            @PathVariable Long projectId,
            @PathVariable Long processId,
            @PathVariable Long taskItemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody WeekMissionTaskItemUpdateReqDto req
    ) {
        return ApiResponse.ok(
                weekMissionService.updateWeekMissionTaskItem(projectId, userDetails.getUserId(), processId, taskItemId, req)
        );
    }

}
