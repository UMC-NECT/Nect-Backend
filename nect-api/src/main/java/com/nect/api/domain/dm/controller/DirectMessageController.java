package com.nect.api.domain.dm.controller;

import com.nect.api.domain.dm.dto.DmMessageSendRequestDto;
import com.nect.api.domain.dm.service.DmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DirectMessageController {

    private final DmService dmService;

    @MessageMapping("/chat-send/dms/{userId}")
    public void sendDm(@DestinationVariable("userId") Long receiverId, DmMessageSendRequestDto request, Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        dmService.sendMessage(senderId, receiverId, request.getContent());

        log.info(" DM 메시지 수신 - senderId: {}, receiverId: {}", senderId, receiverId);
    }

    @MessageMapping("/dm-leave/{userId}")
    public void leaveDmRoom(@DestinationVariable("userId") Long otherUserId, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        dmService.leaveRoom(userId, otherUserId);
        log.info(" DM 방 나가기 - userId: {}, otherUserId: {}", userId, otherUserId);
    }

}
