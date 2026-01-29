package com.nect.api.domain.matching.controller;

import com.nect.api.domain.matching.dto.MatchingReqDto;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.enums.MatchingBox;
import com.nect.api.domain.matching.facade.MatchingFacade;
import com.nect.api.domain.matching.service.MatchingService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matchings")
public class MatchingController {

    private final MatchingService matchingService;
    private final MatchingFacade matchingFacade;

    // user -> project 매칭 요청
    @PostMapping("/projects/{projectId}")
    public ApiResponse<MatchingResDto.MatchingRes> requestMatchingByUser(
            @PathVariable @Positive Long projectId,
            @RequestBody @Valid MatchingReqDto.matchingReqDto reqDto,
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        // TODO: 로그인 구현 이후 삭제 예정
        Long userId = (user!=null) ? user.getUserId() : 1L;
        return ApiResponse.ok(matchingFacade.createUserToProjectMatching(userId, projectId, reqDto.fieldId()));
    }

    // project -> user 매칭 요청
    @PostMapping("/projects/{projectId}/users/{targetUserId}")
    public ApiResponse<MatchingResDto.MatchingRes> requestMatchingByProject(
            @PathVariable @Positive Long projectId,
            @PathVariable @Positive Long targetUserId,
            @RequestBody @Valid MatchingReqDto.matchingReqDto reqDto,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        Long userId = (user!=null) ? user.getUserId() : 1L;
        return ApiResponse.ok(matchingFacade.createProjectToUserMatching(userId, targetUserId, projectId, reqDto.fieldId()));
    }

    // 매칭 요청 취소 (user -> project, project -> user 범용 컨트롤러)
    @PostMapping("/{matchingId}/cancel")
    public ApiResponse<MatchingResDto.MatchingRes> cancelMatchingRequestByUser(
            @PathVariable @Positive Long matchingId,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        Long userId = (user!=null) ? user.getUserId() : 1L;
        return ApiResponse.ok(matchingService.cancelMatching(matchingId, userId));
    }

    // 매칭 요청 수락 (project -> user 매칭의 수락)
    @PostMapping("/{matchingId}/accept")
    public ApiResponse<MatchingResDto.MatchingAcceptResDto> acceptMatchingRequest(
            @PathVariable @Positive Long matchingId,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        Long userId = (user!=null) ? user.getUserId() : 1L;
        return ApiResponse.ok(matchingFacade.acceptMatchingRequest(matchingId, userId));
    }

    @PostMapping("/{matchingId}/reject")
    public ApiResponse<MatchingResDto.MatchingRes> rejectMatchingRequest(
            @PathVariable @Positive Long matchingId,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        Long userId = (user!=null) ? user.getUserId() : 1L;
        return ApiResponse.ok(matchingFacade.rejectMatching(matchingId, userId));
    }
    // 매칭 요청 조회 (received, sent)
    @GetMapping()
    public ApiResponse<MatchingResDto.MatchingListRes> getMatchings(
            @RequestParam String box,
            @AuthenticationPrincipal UserDetailsImpl user
            ){
        MatchingBox matchingBox = MatchingBox.from(box);
        Long userId = (user!=null) ? user.getUserId() : 1L;
        return ApiResponse.ok(matchingService.getMatchingsByBox(userId, matchingBox));
    }
}
