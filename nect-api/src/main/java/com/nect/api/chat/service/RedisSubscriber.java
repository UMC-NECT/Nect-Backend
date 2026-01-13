package com.nect.api.chat.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.chat.dto.ChatMessageDto;
import com.nect.api.chat.enums.ChatErrorCode;
import com.nect.api.chat.exeption.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    // Json<-> 자바 객체 문자열 변환 도구
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public void onMessage(String message, String channel) {
        try {
            log.info("Redis 메시지 수신 - Channel: {}", channel);

            // 받은 메시지 ChatMessageDto 변환 json-> DTO
            ChatMessageDto chatMessage = objectMapper.readValue(message, ChatMessageDto.class);

            // WebSocket으로 구독자들에게 전송
            messagingTemplate.convertAndSend(
                    "/topic/room/" + chatMessage.getRoomId(),
                    chatMessage
            );

            log.info("WebSocket 메시지 전송 완료 - RoomId: {}, Sender: {}",
                    chatMessage.getRoomId(), chatMessage.getSenderName());

        } catch (Exception e) {
            throw new ChatException(
                    ChatErrorCode.REDIS_SUBSCRIBE_FAILED,
                    "Channel: " + channel, e
            );
        }
    }
}