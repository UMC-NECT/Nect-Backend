package com.nect.api.domain.team.chat.controller;

import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.dto.req.ChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.req.GroupChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDTO;
import com.nect.api.domain.team.chat.service.TeamChatService;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chats/rooms")
@RequiredArgsConstructor
public class TeamChatController {

    private final TeamChatService teamChatService;


    @GetMapping("/{projectId}/users")
    public ApiResponse<List<ProjectMemberResponseDTO>> getProjectMembers(
            @PathVariable Long projectId,
                     @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;//TODO 토큰 없을 시 임시 방어 코드
        List<ProjectMemberResponseDTO> response = teamChatService.getProjectMembers(projectId);

        return ApiResponse.ok(response);
    }

    @PostMapping("/personal")
    public ApiResponse<ChatRoomResponseDTO> createPersonalChatRoom(
            @RequestBody ChatRoomCreateRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {



        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;//TODO 토큰 없을 시 임시 방어 코드

        ChatRoomResponseDTO response = teamChatService.createOneOnOneChatRoom(currentUserId, request);
        return ApiResponse.ok(response);
    }


    @PostMapping("/group")
    public ApiResponse<ChatRoomResponseDTO> createGroupChatRoom(
            @RequestBody GroupChatRoomCreateRequestDTO request
            , @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;//TODO 토큰 없을 시 임시 방어 코드

        ChatRoomResponseDTO response = teamChatService.createGroupChatRoom(currentUserId, request);
        return ApiResponse.ok(response);
    }

}