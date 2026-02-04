package com.nect.api.domain.matching.controller;

import com.nect.api.domain.matching.dto.MatchingReqDto;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.enums.CounterParty;
import com.nect.api.domain.matching.facade.MatchingFacade;
import com.nect.api.domain.matching.service.MatchingService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.matching.enums.MatchingStatus;
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
        return ApiResponse.ok(matchingFacade.createUserToProjectMatching(user.getUserId(), projectId, reqDto));
    }

    // project -> user 매칭 요청
    @PostMapping("/projects/{projectId}/users/{targetUserId}")
    public ApiResponse<MatchingResDto.MatchingRes> requestMatchingByProject(
            @PathVariable @Positive Long projectId,
            @PathVariable @Positive Long targetUserId,
            @RequestBody @Valid MatchingReqDto.matchingReqDto reqDto,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        return ApiResponse.ok(matchingFacade.createProjectToUserMatching(user.getUserId(), targetUserId, projectId, reqDto));
    }

    // 매칭 요청 취소 (user -> project, project -> user 범용 컨트롤러)
    @PostMapping("/{matchingId}/cancel")
    public ApiResponse<MatchingResDto.MatchingRes> cancelMatchingRequestByUser(
            @PathVariable @Positive Long matchingId,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        return ApiResponse.ok(matchingFacade.cancelMatching(matchingId, user.getUserId()));
    }

    // 매칭 요청 수락 (project -> user 매칭의 수락)
    @PostMapping("/{matchingId}/accept")
    public ApiResponse<MatchingResDto.MatchingAcceptResDto> acceptMatchingRequest(
            @PathVariable @Positive Long matchingId,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        return ApiResponse.ok(matchingFacade.acceptMatchingRequest(matchingId, user.getUserId()));
    }

    // 매칭 요청 거절
    @PostMapping("/{matchingId}/reject")
    public ApiResponse<MatchingResDto.MatchingRes> rejectMatchingRequest(
            @PathVariable @Positive Long matchingId,
            @RequestBody @Valid MatchingReqDto.matchingRejectReqDto rejectReqDto,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        return ApiResponse.ok(matchingFacade.rejectMatching(matchingId, user.getUserId(), rejectReqDto.rejectReason()));
    }

    // 받은 매칭 요청 조회
    @GetMapping("/received")
    public ApiResponse<MatchingResDto.MatchingListRes> getReceivedMatchings(
            @RequestParam String target,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetailsImpl user
            ){
        CounterParty counterParty = CounterParty.from(target);
        MatchingStatus matchingStatus = MatchingStatus.from(status);
        return ApiResponse.ok(matchingService.getReceivedMatchingsByTarget(user.getUserId(), counterParty, matchingStatus));
    }

    // 보낸 매칭 요청 조회r
    @GetMapping("/sent")
    public ApiResponse<MatchingResDto.MatchingListRes> getSentMatchings(
            @RequestParam String target,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        CounterParty counterParty = CounterParty.from(target);
        MatchingStatus matchingStatus = MatchingStatus.from(status);

        return ApiResponse.ok(matchingService.getSentMatchingsByTarget(user.getUserId(), counterParty, matchingStatus));
    }

    // 매칭 요청 개수 조회
    @GetMapping("/count")
    public ApiResponse<MatchingResDto.MatchingCounts> getMatchingsCount(
            @AuthenticationPrincipal UserDetailsImpl user
    ){
        return ApiResponse.ok(matchingService.getMatchingsCount(user.getUserId()));
    }
}
