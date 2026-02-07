package com.nect.api.domain.team.chat.controller;


import com.nect.api.domain.team.chat.dto.req.ChatMessageSendRequestDto;
import com.nect.api.domain.team.chat.service.ChatFileService;
import com.nect.api.domain.team.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final ChatService chatService;
    private final ChatFileService chatFileService;


    @MessageMapping("/chat-send/{room_id}")
    public void sendMessage(
            @DestinationVariable("room_id") Long room_id,
            ChatMessageSendRequestDto request,
            Principal principal
    ) {
        Long currentUserId = Long.valueOf(principal.getName());
        log.info(" WebSocket 메시지 수신 - roomId: {}, user_id: {}, content: {}",
                room_id, request.getUserId(), request.getContent());

        try {
            chatService.sendMessage(room_id, currentUserId, request.getContent());
        } catch (Exception e) {
            log.error(" 메시지 전송 실패 - roomId: {}, error: {}", room_id, e.getMessage(), e);
        }
    }




}
