package com.nect.api.domain.team.chat.controller;


import com.nect.api.domain.team.chat.dto.req.ChatNotificationUpdateRequestDto;
import com.nect.api.domain.team.chat.dto.req.ChatRoomInviteRequestDto;
import com.nect.api.domain.team.chat.dto.res.ChatNotificationResponseDto;
import com.nect.api.domain.team.chat.service.ChatRoomService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.dto.req.ChatRoomCreateRequestDto;
import com.nect.api.domain.team.chat.dto.req.GroupChatRoomCreateRequestDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDto;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDto;
import com.nect.api.domain.team.chat.service.TeamChatService;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats/rooms")
@RequiredArgsConstructor
public class TeamChatController {

    private final TeamChatService teamChatService;
    private final ChatRoomService chatRoomService;


    @GetMapping("/{projectId}/users")
    public ApiResponse<List<ProjectMemberResponseDto>> getProjectMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;
        List<ProjectMemberResponseDto> response = teamChatService.getProjectMembers(projectId);

        return ApiResponse.ok(response);
    }

    @PostMapping("/personal")
    public ApiResponse<ChatRoomResponseDto> createPersonalChatRoom(
            @RequestBody ChatRoomCreateRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {



        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;

        ChatRoomResponseDto response = teamChatService.createOneOnOneChatRoom(currentUserId, request);
        return ApiResponse.ok(response);
    }


    @PostMapping("/group")
    public ApiResponse<ChatRoomResponseDto> createGroupChatRoom(
            @RequestBody GroupChatRoomCreateRequestDto request
            , @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;

        ChatRoomResponseDto response = teamChatService.createGroupChatRoom(currentUserId, request);
        return ApiResponse.ok(response);
    }

    @PatchMapping("/rooms/{room_id}/notification")
    public ApiResponse<ChatNotificationResponseDto> updateNotificationSettings(
            @PathVariable("room_id") Long room_id,
            @RequestBody ChatNotificationUpdateRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {


        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;

        ChatNotificationResponseDto response = chatRoomService.updateNotificationSettings(room_id, currentUserId, request.getIsNotificationEnabled());

        return ApiResponse.ok(response);
    }

    @PostMapping("/{roomId}/invite")
    public ApiResponse<Void> inviteMembers(
            @PathVariable Long roomId,
            @RequestBody ChatRoomInviteRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;
        teamChatService.inviteMembers(roomId, currentUserId, request);

        return ApiResponse.ok(null);
    }


}