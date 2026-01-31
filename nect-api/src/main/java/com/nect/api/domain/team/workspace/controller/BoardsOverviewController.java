package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.dto.res.BoardsOverviewResDto;
import com.nect.api.domain.team.workspace.facade.BoardsOverviewFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.team.workspace.enums.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/boards")
public class BoardsOverviewController {

    private final BoardsOverviewFacade facade;

    /**
     * 팀보드 통합(전체) 조회
     * - 팀보드 화면에 필요한 카드들을 한 번에 내려준다.
     * - calendar_month_indicators: year/month 파라미터가 들어올 때만 내려줌(없으면 null)
     */
    @GetMapping("/overview")
    public ApiResponse<BoardsOverviewResDto> getOverview(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,

            // calendar indicator는 필요한 화면에서만 호출하도록 optional로
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,

            // 프리뷰
            @RequestParam(required = false) String from,                 // upcoming schedules 기준일 (yyyy-MM-dd)
            @RequestParam(defaultValue = "6") int scheduleLimit,
            @RequestParam(defaultValue = "4") int docsLimit,
            @RequestParam(defaultValue = "4") int postsLimit,

            // 게시판 프리뷰 타입(공지/자유 등) - 없으면 facade에서 기본값 처리
            @RequestParam(required = false) PostType postType
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(
                facade.getOverview(projectId, userId, year, month, from, scheduleLimit, docsLimit, postsLimit, postType)
        );
    }
}
