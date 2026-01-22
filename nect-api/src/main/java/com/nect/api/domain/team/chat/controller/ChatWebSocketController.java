package com.nect.api.domain.team.chat.controller;


import com.nect.api.domain.team.chat.dto.req.ChatFileSendRequestDTO;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDTO;
import com.nect.api.domain.team.chat.dto.req.ChatMessageSendRequestDTO;
import com.nect.api.domain.team.chat.service.ChatFileService;
import com.nect.api.domain.team.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final ChatService chatService;
    private final ChatFileService chatFileService;

    @MessageMapping("/chat.send/{room_id}")
    public void sendMessage(
            @DestinationVariable("room_id") Long room_id,
            ChatMessageSendRequestDTO request
    ) {
        Long user_id = (request.getUserId() != null) ? request.getUserId() : 1L; //TODO 실시간 채팅 테스트용 user_id 1또는 2

        log.info(" WebSocket 메시지 수신 - roomId: {}, user_id: {}, content: {}",
                room_id, request.getUserId(), request.getContent());

        try {

            ChatMessageDTO message = chatService.sendMessage(
                    room_id,
                    user_id, //TODO 실시간 채팅 테스트용 user_id 1또는 2
                    request.getContent()
            );

            log.info("메시지 처리 완료 - messageId: {}", message.getMessageId());

        } catch (Exception e) {
            log.error(" 메시지 전송 실패 - roomId: {}, error: {}", room_id, e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.file/{room_id}")
    public void sendFileMessage(
            @DestinationVariable("room_id") Long roomId,
            ChatFileSendRequestDTO request
    ) {
        Long userId = (request.getUserId() != null) ? request.getUserId() : 1L;

        log.info("파일 전송 요청 - room: {}, fileId: {}", roomId, request.getFileId());

        try {
            chatFileService.sendFileMessage(roomId, userId, request.getFileId());
        } catch (Exception e) {
            log.error("파일 전송 실패", e);
        }
    }


}
