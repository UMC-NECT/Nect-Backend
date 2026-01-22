package com.nect.api.domain.team.chat.controller;

import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.dto.req.ChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.req.GroupChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDTO;
import com.nect.api.domain.team.chat.service.TeamChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            @PathVariable Long projectId
            // @AuthenticationPrincipal UserDetailsImpl userDetails // TODO 추후 권한 체크 시 사용
    ) {

        List<ProjectMemberResponseDTO> response = teamChatService.getProjectMembers(projectId);

        return ApiResponse.ok(response);
    }

    @PostMapping("/personal")
    public ApiResponse<ChatRoomResponseDTO> createPersonalChatRoom(
            @RequestBody ChatRoomCreateRequestDTO request
            //@AuthenticationPrincipal UserDetailsImpl userDetails // TODO 추후 권한 체크 시 사용
    ) {

        //TODO 임시 하드코딩 수정 필요
        //Long currentUserId = userDetails.getUserId();
        Long currentUserId = 1L;

        ChatRoomResponseDTO response = teamChatService.createOneOnOneChatRoom(currentUserId, request);
        return ApiResponse.ok(response);
    }


    @PostMapping("/group")
    public ApiResponse<ChatRoomResponseDTO> createGroupChatRoom(
            @RequestBody GroupChatRoomCreateRequestDTO request
           // , @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        //TODO 임시 하드코딩 수정 필요
        //Long currentUserId = userDetails.getUserId();
        Long currentUserId = 2L;
        ChatRoomResponseDTO response = teamChatService.createGroupChatRoom(currentUserId, request);
        return ApiResponse.ok(response);
    }

}