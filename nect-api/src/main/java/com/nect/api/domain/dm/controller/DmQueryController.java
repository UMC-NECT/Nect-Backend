package com.nect.api.domain.dm.controller;

import com.nect.api.domain.dm.dto.DmMessageListResponse;
import com.nect.api.domain.dm.dto.DmRoomListResponse;
import com.nect.api.domain.dm.service.DmService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dms")
public class DmQueryController {

    private final DmService dmService;

    @GetMapping("/messages")
    public ApiResponse<DmMessageListResponse> getMessages(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("userId") Long otherUserId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        DmMessageListResponse response = dmService.getMessages(userDetails.getUserId(), otherUserId, cursor, size);
        return ApiResponse.ok(response);
    }

    @GetMapping("/rooms")
    public ApiResponse<DmRoomListResponse> getRooms(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        DmRoomListResponse response = dmService.getRooms(userDetails.getUserId(), cursor, size);
        return ApiResponse.ok(response);
    }
}
