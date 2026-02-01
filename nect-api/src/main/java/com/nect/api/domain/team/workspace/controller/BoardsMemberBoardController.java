package com.nect.api.domain.team.workspace.controller;

import com.nect.api.domain.team.workspace.dto.res.MemberBoardResDto;
import com.nect.api.domain.team.workspace.facade.BoardsMemberBoardFacade;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/boards")
public class BoardsMemberBoardController {

    private final BoardsMemberBoardFacade facade;

    /**
     * 팀보드 좌측 하단 "팀원 프로필 보드" 조회
     * - 프로젝트 멤버들의 기본 정보 + 멤버별 담당 프로세스 진행 상태 카운트(진행 전/중/완료)
     * - profile_image_url TODO로 내려줌
     */
    @GetMapping("/members")
    public ApiResponse<MemberBoardResDto> getMemberBoard(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ApiResponse.ok(facade.getMemberBoard(projectId, userId));
    }
}