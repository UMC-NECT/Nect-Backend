package com.nect.api.domain.team.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message; // import 주의
import org.springframework.data.redis.connection.MessageListener; // import 주의
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber implements MessageListener { // MessageListener 인터페이스 구현

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            //  Redis에서 온 메시지 String으로 변환
            String publishMessage = new String(message.getBody());

            log.info(" Redis Subscriber 수신 - Message: {}", publishMessage);

            //  JSON 문자열 -> DTO 변환
            ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);

            //  WebSocket 구독자들에게 전송
            String destination = "/topic/chatroom/" + chatMessage.getRoomId();

            messagingTemplate.convertAndSend(destination, chatMessage);

            log.info(" WebSocket 전송 완료 - Destination: {}", destination);

        } catch (Exception e) {
            log.error(" Redis 메시지 처리 실패", e);
            throw new ChatException(ChatErrorCode.REDIS_SUBSCRIBE_FAILED, "Message handling failed", e);
        }
    }
}