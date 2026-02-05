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

public class RedisSubscriber implements MessageListener { // MessageListener 인터페이스 구현

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = new String(message.getBody());


            ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);

            String destination = "/topic/chatroom/" + chatMessage.getRoomId();

            messagingTemplate.convertAndSend(destination, chatMessage);


        } catch (Exception e) {
            throw new ChatException(ChatErrorCode.REDIS_SUBSCRIBE_FAILED, "Message handling failed", e);
        }
    }
}