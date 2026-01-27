package com.nect.api.domain.team.chat.controller;

import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDTO;
import com.nect.api.domain.team.chat.dto.req.ChatNoticeUpdateRequestDTO;
import com.nect.api.domain.team.chat.dto.res.ChatNoticeResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomLeaveResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomListDTO;
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
@RequestMapping("/chats")
public class ChatMessageController {

    private final ChatFacade chatFacade;
    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    @GetMapping("/rooms/{room_id}/messages")
    public ApiResponse<List<ChatMessageDTO>> getChatMessages(
            @PathVariable Long room_id,
            @RequestParam(required = false) Long lastMessage_id,
            @RequestParam(defaultValue = "20") int size,
           @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {


        List<ChatMessageDTO> messages = chatService.getChatMessages(room_id, lastMessage_id, size);
        return ApiResponse.ok(messages);
    }


    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomListDTO>> getChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;

        List<ChatRoomListDTO> myRooms = chatRoomService.getMyChatRooms(currentUserId);
        return ApiResponse.ok(myRooms);
    }


    @DeleteMapping("/{room_id}/leave")
    public ApiResponse<ChatRoomLeaveResponseDTO> leaveChatRoom(
            @PathVariable("room_id") Long roomId,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        // TODO 임시 방어 코드
        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : 1L;

        ChatRoomLeaveResponseDTO response = chatRoomService.leaveChatRoom(roomId, currentUserId);

        return ApiResponse.ok(response);
    }

    @PatchMapping("/message/{message_id}/notice")
    public ApiResponse<ChatNoticeResponseDTO>updateNotice(@PathVariable("message_id") Long messageId, @RequestBody ChatNoticeUpdateRequestDTO request
    ,@AuthenticationPrincipal UserDetailsImpl userDetails){

        ChatNoticeResponseDTO response=chatService.createNotice(messageId,request.getIsPinned());
        return ApiResponse.ok(response);
    }





}
