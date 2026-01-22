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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            @RequestParam(required = false) Long lastMessage_id
            //TODO @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        Long user_id = 1L; //TODO [현재] 임시 하드코딩
        //TODO Long userId = userDetails.getUser().getId();
        List<ChatMessageDTO> messages = chatService.getChatMessages(room_id, user_id, lastMessage_id);

        return ApiResponse.ok(messages);
    }


    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomListDTO>> getChatRoom(@RequestParam Long user_id) {
        List<ChatRoomListDTO> myRooms = chatRoomService.getMyChatRooms(user_id);
        return ApiResponse.ok(myRooms);
    }


    @DeleteMapping("/{room_id}/leave")
    public ApiResponse<ChatRoomLeaveResponseDTO> leaveChatRoom(
            @PathVariable("room_id") Long roomId,
            @RequestParam(required = false) Long userId // TODO: 추후 Security로 대체
    ) {
       //TODO 임시 테스트용 하드코딩
        Long currentUserId = (userId != null) ? userId : 1L;

        ChatRoomLeaveResponseDTO response = chatRoomService.leaveChatRoom(roomId, currentUserId);

        return ApiResponse.ok(response);
    }

    @PatchMapping("/message/{message_id}/notice")
    public ApiResponse<ChatNoticeResponseDTO>updateNotice(@PathVariable("message_id") Long messageId, @RequestBody ChatNoticeUpdateRequestDTO request){

        ChatNoticeResponseDTO response=chatService.createNotice(messageId,request.getIsPinned());
        return ApiResponse.ok(response);
    }





}
