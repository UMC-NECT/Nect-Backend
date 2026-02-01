package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.dto.req.BoardsBasicInfoUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.BoardsBasicInfoGetResDto;
import com.nect.api.domain.team.workspace.dto.res.MissionProgressResDto;
import com.nect.api.domain.team.workspace.facade.BoardsFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/projects/{projectId}/boards")
public class BoardsController {
    private final BoardsFacade boardsFacade;

    // 기본 정보 조회
    @GetMapping("/basic-info")
    public ApiResponse<BoardsBasicInfoGetResDto> getBasicInfo(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(boardsFacade.getBoardsBasicInfo(projectId, userId));
    }

    // 기본 정보 수정
    @PatchMapping("/basic-info")
    public ApiResponse<Void> updateBasicInfo(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody BoardsBasicInfoUpdateReqDto req
    ) {
        Long userId = userDetails.getUserId();
        boardsFacade.updateBasicInfo(projectId, userId, req);
        return ApiResponse.ok(null);
    }

    // 미션 완료 개수 조회
    @GetMapping("/mission-progress")
    public ApiResponse<MissionProgressResDto> getMissionProgress(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(boardsFacade.getMissionProgress(projectId, userId));
    }
}
