package com.nect.api.domain.team.chat.controller;

import com.nect.api.domain.team.chat.dto.res.*;
import com.nect.api.domain.team.chat.service.TeamChatService;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.dto.req.ChatNoticeUpdateRequestDto;
import com.nect.api.domain.team.chat.facade.ChatFacade;
import com.nect.api.domain.team.chat.service.ChatRoomService;
import com.nect.api.domain.team.chat.service.ChatService;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/chats")
public class ChatMessageController {

    private final ChatFacade chatFacade;
    private final ChatRoomService chatRoomService;
    private final ChatService chatService;
    private final TeamChatService  teamChatService;

    // 방 메시지 조회
    @GetMapping("/rooms/{room_id}/messages")
    public ApiResponse<ChatRoomMessagesResponseDto> getChatMessages(
            @PathVariable Long room_id,
            @RequestParam(required = false) Long lastMessage_id,
            @RequestParam(defaultValue = "20") int size,
           @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = userDetails.getUserId();
       ChatRoomMessagesResponseDto  messages = chatService.getChatMessages(room_id, currentUserId,lastMessage_id, size);
        return ApiResponse.ok(messages);
    }



    @GetMapping("/projects/{projectId}/rooms")
    public ApiResponse<List<ChatRoomListDto>> getProjectChatRooms(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId = userDetails.getUserId();


        List<ChatRoomListDto> rooms = chatRoomService.getProjectChatRooms(projectId, userId);

        return ApiResponse.ok(rooms);
    }


    @DeleteMapping("/{room_id}/leave")
    public ApiResponse<ChatRoomLeaveResponseDto> leaveChatRoom(
            @PathVariable("room_id") Long roomId,
            @AuthenticationPrincipal UserDetailsImpl userDetails){

        Long currentUserId = userDetails.getUserId();

        ChatRoomLeaveResponseDto response =  chatRoomService.leaveChatRoom(roomId, currentUserId);

        return ApiResponse.ok(response);
    }

    // 공지사항
    @PatchMapping("/message/{message_id}/notice")
    public ApiResponse<ChatNoticeResponseDto>updateNotice(@PathVariable("message_id") Long messageId, @RequestBody ChatNoticeUpdateRequestDto request
    , @AuthenticationPrincipal UserDetailsImpl userDetails){

        ChatNoticeResponseDto response = chatService.createNotice(
                messageId,
                request.getIsPinned(),
                userDetails.getUserId()
        );
        return ApiResponse.ok(response);
    }

    // 방생성 페이지 프로젝트 인원 조회,선택,검색
    @GetMapping("/projects/{projectId}/members")
    public ApiResponse<List<ProjectMemberDto>> getProjectMembers(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = userDetails.getUserId();

        List<ProjectMemberDto> members = teamChatService.getProjectMembers(projectId, currentUserId,keyword);

        return ApiResponse.ok(members);
    }


    // 채팅방 메시지 검색
    @GetMapping("/rooms/{room_id}/messages/search")
    public ApiResponse<ChatMessageSearchResponseDto> searchMessages(
            @PathVariable Long room_id,
            @RequestParam String keyword,  // 검색어만
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = userDetails.getUserId();

        ChatMessageSearchResponseDto response = chatService.searchMessages(
                room_id,
                currentUserId,
                keyword,
                page,
                size
        );

        return ApiResponse.ok(response);
    }

}
